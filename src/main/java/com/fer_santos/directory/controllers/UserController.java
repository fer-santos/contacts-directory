package com.fer_santos.directory.controllers;

import com.fer_santos.directory.utils.DatabaseManager;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class UserController {

    public static class UpdateUserDto {
        public String firstName;
        public String lastName;
        public String email;
    }

    public static class UpdatePasswordDto {
        public String newPassword;
    }

    public static void getUserProfile(Context ctx) {
        String userId = ctx.attribute("userId");
        if (userId == null) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        String sql = "SELECT firstName, lastName, email FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ctx.json(Map.of(
                        "firstName", rs.getString("firstName"),
                        "lastName", rs.getString("lastName") != null ? rs.getString("lastName") : "",
                        "email", rs.getString("email")
                    ));
                } else {
                    ctx.status(404).result("User not found");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).result("Database error");
        }
    }

    public static void updateUserProfile(Context ctx) {
        String userId = ctx.attribute("userId");
        if (userId == null) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        UpdateUserDto dto = ctx.bodyAsClass(UpdateUserDto.class);

        if (dto.firstName == null || dto.firstName.trim().isEmpty() || 
            dto.email == null || dto.email.trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "First Name and Email cannot be empty"));
            return;
        }

        // Check if email is used by another user
        String emailCheckSql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) AND id != ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(emailCheckSql)) {
            pstmt.setString(1, dto.email);
            pstmt.setString(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ctx.status(400).json(Map.of("error", "Email already in use"));
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).result("Database error");
            return;
        }

        // Update user
        String sql = "UPDATE users SET firstName = ?, lastName = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dto.firstName);
            pstmt.setString(2, dto.lastName);
            pstmt.setString(3, dto.email);
            pstmt.setString(4, userId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ctx.status(200).json(Map.of("message", "Profile updated successfully"));
            } else {
                ctx.status(404).result("User not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).result("Database error");
        }
    }

    public static void updateUserPassword(Context ctx) {
        String userId = ctx.attribute("userId");
        if (userId == null) {
            ctx.status(401).result("Unauthorized");
            return;
        }

        UpdatePasswordDto dto = ctx.bodyAsClass(UpdatePasswordDto.class);
        if (dto.newPassword == null || dto.newPassword.trim().length() < 8) {
            ctx.status(400).json(Map.of("error", "Password must be at least 8 characters"));
            return;
        }

        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dto.newPassword);
            pstmt.setString(2, userId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ctx.status(200).json(Map.of("message", "Password updated successfully"));
            } else {
                ctx.status(404).result("User not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(500).result("Database error");
        }
    }
}
