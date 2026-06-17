package com.fer_santos.directory;

import com.fer_santos.directory.models.Contact;
import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.StorageManager;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
    app.get("/api/contacts", ctx -> {
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
    });

    app.post("/api/contacts", ctx -> {
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
    });

    app.put("/api/contacts", ctx -> {
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
    });

    app.delete("/api/contacts", ctx -> {
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
    });
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
