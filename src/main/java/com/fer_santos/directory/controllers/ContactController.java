package com.fer_santos.directory.controllers;

import com.fer_santos.directory.models.Contact;
import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.StorageManager;
import io.javalin.http.Context;

import java.util.ArrayList;

public class ContactController {

    public static void getAllContacts(Context ctx) {
      String email = ctx.attribute("email");
      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) {
        ctx.status(500).result("Error loading database.");
        return;
      }

      User owner = currentUsers.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
      if (owner == null) {
        ctx.status(404).result("User not found.");
        return;
      }

      ctx.json(owner.getContacts());
    }

    public static void createContact(Context ctx) {
      String email = ctx.attribute("email");
      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) {
        ctx.status(500).result("Error loading database.");
        return;
      }

      try {
        Contact newContact = ctx.bodyAsClass(Contact.class);

        if (newContact.getName() == null || newContact.getName().isBlank() ||
            newContact.getPhoneNumber() == null || newContact.getPhoneNumber().isBlank()) {
          ctx.status(400).result("Fields 'name' and 'phoneNumber' cannot be null or empty.");
          return;
        }

        User owner = currentUsers.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);
        if (owner == null) {
          ctx.status(404).result("User not found.");
          return;
        }

        owner.getContacts().add(newContact);
        StorageManager.saveUsers(currentUsers);
        ctx.status(201).json(newContact);

      } catch (Exception e) {
        ctx.status(400).result("Invalid JSON body or format.");
      }
    }

    public static void updateContact(Context ctx) {
      String email = ctx.attribute("email");
      String id = ctx.queryParam("id");

      if (id == null || id.isBlank()) {
        ctx.status(400).result("Parameter 'id' is mandatory.");
        return;
      }

      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) {
        ctx.status(500).result("Error loading database.");
        return;
      }

      try {
        User user = currentUsers.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);

        if (user == null) {
          ctx.status(404).result("User not found.");
          return;
        }

        int targetIndex = -1;
        for (int i = 0; i < user.getContacts().size(); i++) {
          Contact c = user.getContacts().get(i);
          if (c.getId() != null && c.getId().equals(id)) {
            targetIndex = i;
            break;
          }
        }

        if (targetIndex == -1) {
          ctx.status(404).result("Contact not found.");
          return;
        }

        Contact updatedContact = ctx.bodyAsClass(Contact.class);
        if (updatedContact.getName() == null || updatedContact.getName().isBlank() ||
            updatedContact.getPhoneNumber() == null || updatedContact.getPhoneNumber().isBlank()) {
          ctx.status(400).result("Fields 'name' and 'phoneNumber' are mandatory.");
          return;
        }

        // Ensure ID is maintained
        updatedContact.setId(id);

        user.getContacts().set(targetIndex, updatedContact);
        StorageManager.saveUsers(currentUsers);
        ctx.status(200).json(updatedContact);

      } catch (Exception e) {
        ctx.status(500).result("Persistence error.");
      }
    }

    public static void deleteContact(Context ctx) {
      String email = ctx.attribute("email");
      String id = ctx.queryParam("id");

      if (id == null || id.isBlank()) {
        ctx.status(400).result("Parameter 'id' is mandatory.");
        return;
      }

      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) {
        ctx.status(500).result("Error loading database.");
        return;
      }

      try {
        User user = currentUsers.stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst().orElse(null);

        if (user == null) {
          ctx.status(404).result("User not found.");
          return;
        }

        boolean removed = user.getContacts().removeIf(c -> c.getId() != null && c.getId().equals(id));

        if (!removed) {
          ctx.status(404).result("Contact not found.");
          return;
        }

        StorageManager.saveUsers(currentUsers);
        ctx.status(200).result("Contact deleted successfully.");

      } catch (Exception e) {
        ctx.status(500).result("Persistence error.");
      }
    }
}