package com.fer_santos.directory.controllers;

import com.fer_santos.directory.models.Contact;
import com.fer_santos.directory.utils.DatabaseManager;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ContactController {

    public static void getAllContacts(Context ctx) {
      String userId = ctx.attribute("userId");
      
      if (userId == null) {
        ctx.status(401).result("Unauthorized");
        return;
      }

      List<Contact> contacts = new ArrayList<>();
      String sql = "SELECT id, firstName, lastName, email, phoneNumber, alias, isFavorite, isTrashed FROM contacts WHERE user_id = ?";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
           pstmt.setString(1, userId);
           try (ResultSet rs = pstmt.executeQuery()) {
               while (rs.next()) {
                   Contact c = new Contact();
                   c.setId(rs.getString("id"));
                   c.setName(rs.getString("firstName"));
                   c.setLastName(rs.getString("lastName"));
                   c.setEmail(rs.getString("email"));
                   c.setPhoneNumber(rs.getString("phoneNumber"));
                   c.setAlias(rs.getString("alias"));
                   c.setFavorite(rs.getInt("isFavorite") == 1);
                   c.setTrashed(rs.getInt("isTrashed") == 1);
                   contacts.add(c);
               }
           }
           ctx.json(contacts);
      } catch (SQLException e) {
           ctx.status(500).result("Error loading contacts from database.");
      }
    }

    public static void createContact(Context ctx) {
      String userId = ctx.attribute("userId");
      if (userId == null) {
        ctx.status(401).json(Map.of("error", "Unauthorized"));
        return;
      }

      try {
        Contact newContact = ctx.bodyAsClass(Contact.class);

        if (newContact.getName() == null || newContact.getName().trim().isEmpty() ||
            newContact.getPhoneNumber() == null || newContact.getPhoneNumber().trim().isEmpty()) {
          ctx.status(400).json(Map.of("error", "First Name and Phone Number are required"));
          return;
        }

        if (newContact.getLastName() != null && newContact.getLastName().trim().isEmpty()) {
            newContact.setLastName(null);
        }
        if (newContact.getEmail() != null && newContact.getEmail().trim().isEmpty()) {
            newContact.setEmail(null);
        }

        if (newContact.getId() == null) {
            newContact.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO contacts (id, user_id, firstName, lastName, email, phoneNumber, alias, isFavorite, isTrashed) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newContact.getId());
            pstmt.setString(2, userId);
            pstmt.setString(3, newContact.getName());
            pstmt.setString(4, newContact.getLastName());
            pstmt.setString(5, newContact.getEmail());
            pstmt.setString(6, newContact.getPhoneNumber());
            pstmt.setString(7, newContact.getAlias());
            pstmt.setInt(8, newContact.isFavorite() ? 1 : 0);
            pstmt.setInt(9, newContact.isTrashed() ? 1 : 0);
            pstmt.executeUpdate();
            
            ctx.status(201).json(newContact);
        } catch (SQLException e) {
            ctx.status(500).json(Map.of("error", "Database error while saving contact."));
        }

      } catch (Exception e) {
        ctx.status(400).json(Map.of("error", "Invalid JSON body or format."));
      }
    }

    public static void updateContact(Context ctx) {
      String userId = ctx.attribute("userId");
      if (userId == null) {
        ctx.status(401).json(Map.of("error", "Unauthorized"));
        return;
      }
      
      String id = ctx.queryParam("id");
      if (id == null || id.isBlank()) {
        ctx.status(400).result("Parameter 'id' is mandatory.");
        return;
      }

      try {
        Contact updatedContact = ctx.bodyAsClass(Contact.class);
        if (updatedContact.getName() == null || updatedContact.getName().trim().isEmpty() ||
            updatedContact.getPhoneNumber() == null || updatedContact.getPhoneNumber().trim().isEmpty()) {
          ctx.status(400).json(Map.of("error", "First Name and Phone Number are required"));
          return;
        }

        if (updatedContact.getLastName() != null && updatedContact.getLastName().trim().isEmpty()) {
            updatedContact.setLastName(null);
        }
        if (updatedContact.getEmail() != null && updatedContact.getEmail().trim().isEmpty()) {
            updatedContact.setEmail(null);
        }

        updatedContact.setId(id);

        String sql = "UPDATE contacts SET firstName = ?, lastName = ?, email = ?, phoneNumber = ?, alias = ?, isFavorite = ?, isTrashed = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, updatedContact.getName());
            pstmt.setString(2, updatedContact.getLastName());
            pstmt.setString(3, updatedContact.getEmail());
            pstmt.setString(4, updatedContact.getPhoneNumber());
            pstmt.setString(5, updatedContact.getAlias());
            pstmt.setInt(6, updatedContact.isFavorite() ? 1 : 0);
            pstmt.setInt(7, updatedContact.isTrashed() ? 1 : 0);
            pstmt.setString(8, id);
            pstmt.setString(9, userId);
            
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                ctx.status(404).result("Contact not found.");
                return;
            }
            
            ctx.status(200).json(updatedContact);
        } catch (SQLException e) {
            ctx.status(500).json(Map.of("error", "Database error while updating contact."));
        }

      } catch (Exception e) {
        ctx.status(400).json(Map.of("error", "Invalid JSON body or format."));
      }
    }

    public static void deleteContact(Context ctx) {
      String userId = ctx.attribute("userId");
      if (userId == null) {
        ctx.status(401).result("Unauthorized");
        return;
      }
      
      String id = ctx.queryParam("id");
      if (id == null || id.isBlank()) {
        ctx.status(400).result("Parameter 'id' is mandatory.");
        return;
      }

      String sql = "DELETE FROM contacts WHERE id = ? AND user_id = ?";
      try (Connection conn = DatabaseManager.getConnection();
           PreparedStatement pstmt = conn.prepareStatement(sql)) {
          pstmt.setString(1, id);
          pstmt.setString(2, userId);
          int rows = pstmt.executeUpdate();
          if (rows == 0) {
              ctx.status(404).result("Contact not found.");
              return;
          }
          ctx.status(200).result("Contact deleted successfully.");
      } catch (SQLException e) {
          ctx.status(500).result("Persistence error.");
      }
    }
}