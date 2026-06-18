package com.fer_santos.directory;

import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.DatabaseManager;
import com.fer_santos.directory.controllers.ContactController;
import com.fer_santos.directory.controllers.AuthController;
import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

  public static void main(String[] args) {
    DatabaseManager.initialize();

    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
        if (rs.next() && rs.getInt(1) == 0) {
            User admin = new User("Admin", "System", "admin@amber.com", "admin123");
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (id, firstName, lastName, email, password) VALUES (?, ?, ?, ?, ?)")) {
                pstmt.setString(1, admin.getId());
                pstmt.setString(2, admin.getName());
                pstmt.setString(3, admin.getLastName());
                pstmt.setString(4, admin.getEmail());
                pstmt.setString(5, admin.getPassword());
                pstmt.executeUpdate();
            }
            System.out.println("Seed user created: admin@amber.com");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    Javalin app = Javalin.create(config -> {
      config.staticFiles.add("/public");
      config.requestLogger.http((ctx, ms) -> {
        System.out.println("Request: " + ctx.method() + " " + ctx.path() + " - " + ms + "ms");
      });
    }).start(7070);

    // Authentication Endpoints
    app.post("/api/auth/login", AuthController::login);
    app.post("/api/auth/register", AuthController::register);

    // Middleware to protect /api/contacts routes
    app.before("/api/contacts*", ctx -> {
      String authHeader = ctx.header("Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        ctx.status(401).result("Unauthorized");
        return;
      }
      String token = authHeader.substring(7);
      String email = AuthController.activeSessions.get(token);
      
      if (email == null) {
        ctx.status(401).result("Unauthorized");
      } else {
        ctx.attribute("email", email);
      }
    });

    // Contacts API
    app.get("/api/contacts", ContactController::getAllContacts);
    app.post("/api/contacts", ContactController::createContact);
    app.put("/api/contacts", ContactController::updateContact);
    app.delete("/api/contacts", ContactController::deleteContact);
  }
}
