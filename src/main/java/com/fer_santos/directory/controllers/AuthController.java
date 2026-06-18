package com.fer_santos.directory.controllers;

import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.StorageManager;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthController {
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

    public static void login(Context ctx) {
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
    }

    public static void register(Context ctx) {
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
