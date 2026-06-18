package com.fer_santos.directory;

import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.StorageManager;
import com.fer_santos.directory.controllers.ContactController;
import com.fer_santos.directory.controllers.AuthController;
import io.javalin.Javalin;

import java.util.ArrayList;

public class Main {

  public static void main(String[] args) {
    ArrayList<User> loadedUsers = StorageManager.loadUsers();
    final ArrayList<User> usersList = (loadedUsers != null) ? loadedUsers : new ArrayList<>();

    if (usersList.isEmpty()) {
      User admin = new User("Admin", "System", "admin@amber.com", "admin123");
      usersList.add(admin);
      StorageManager.saveUsers(usersList);
      System.out.println("Seed user created: admin@amber.com");
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
