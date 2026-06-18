package com.fer_santos.directory;

import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.StorageManager;
import com.fer_santos.directory.controllers.ContactController;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
  public static Map<String, String> activeSessions = new ConcurrentHashMap<>();

  public static class LoginDto {
    public String email;
    public String password;
  }

  public static class RegisterDto {
    public String firstName;
    public String lastName;
    public String email;
    public String password;
  }

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
    app.post("/api/auth/login", ctx -> {
      LoginDto dto = ctx.bodyAsClass(LoginDto.class);
      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) currentUsers = new ArrayList<>();

      User user = authenticateUser(dto.email, dto.password, currentUsers);
      if (user != null) {
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, user.getEmail());
        ctx.json(Map.of(
            "token", token,
            "name", user.getName(),
            "lastName", user.getLastName() != null ? user.getLastName() : "",
            "email", user.getEmail()
        ));
      } else {
        ctx.status(401).result("Invalid credentials");
      }
    });

    app.post("/api/auth/register", ctx -> {
      RegisterDto dto = ctx.bodyAsClass(RegisterDto.class);
      ArrayList<User> currentUsers = StorageManager.loadUsers();
      if (currentUsers == null) currentUsers = new ArrayList<>();

      if (isEmailRegistered(dto.email, currentUsers)) {
        ctx.status(400).result("Email already registered");
        return;
      }
      
      try {
        User newUser = new User(dto.firstName, dto.lastName, dto.email, dto.password);
        currentUsers.add(newUser);
        StorageManager.saveUsers(currentUsers);
        ctx.status(201).result("Registered successfully");
      } catch (IllegalArgumentException e) {
        ctx.status(400).result(e.getMessage());
      }
    });

    // Middleware to protect /api/contacts routes
    app.before("/api/contacts*", ctx -> {
      String authHeader = ctx.header("Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        ctx.status(401).result("Unauthorized");
        return;
      }
      String token = authHeader.substring(7);
      String email = activeSessions.get(token);
      
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

  public static User authenticateUser(String email, String password, ArrayList<User> userList) {
    for (User currentUser : userList) {
      boolean isEmailCorrect = currentUser.getEmail().equalsIgnoreCase(email);
      boolean isPasswordCorrect = currentUser.getPassword().equals(password);

      if (isEmailCorrect && isPasswordCorrect) return currentUser;
    }
    return null;
  }

  public static boolean isEmailRegistered(String email, ArrayList<User> usersList) {
    for (User user : usersList) {
      if (user.getEmail().equalsIgnoreCase(email)) {
        return true;
      }
    }
    return false;
  }
}
