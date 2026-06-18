package com.fer_santos.directory.controllers;

import com.fer_santos.directory.models.User;
import com.fer_santos.directory.utils.DatabaseManager;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class AuthController {

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

        User user = authenticateUser(dto.email, dto.password);
        if (user != null) {
            Algorithm algorithm = Algorithm.HMAC256("AmberMinimalSecretKey2026");
            String token = JWT.create()
                    .withSubject(user.getId())
                    .withClaim("email", user.getEmail())
                    .sign(algorithm);
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

        if (isEmailRegistered(dto.email)) {
            ctx.status(400).result("Email already registered");
            return;
        }

        try {
            User newUser = new User(dto.firstName, dto.lastName, dto.email, dto.password);
            
            String sql = "INSERT INTO users (id, firstName, lastName, email, password) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newUser.getId());
                pstmt.setString(2, newUser.getName());
                pstmt.setString(3, newUser.getLastName());
                pstmt.setString(4, newUser.getEmail());
                pstmt.setString(5, newUser.getPassword());
                pstmt.executeUpdate();
                ctx.status(201).result("Registered successfully");
            } catch (SQLException e) {
                ctx.status(500).result("Database error while saving user.");
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        }
    }

    public static User authenticateUser(String email, String password) {
        String sql = "SELECT id, firstName, lastName, email, password FROM users WHERE LOWER(email) = LOWER(?) AND password = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getString("firstName"), rs.getString("lastName"), rs.getString("email"), rs.getString("password"));
                    user.setId(rs.getString("id"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isEmailRegistered(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
