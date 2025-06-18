package com.example.demo.controllers;

import com.example.demo.models.UserSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

@Controller
public class AdminController {

    private final DataSource dataSource; // <<< INJECT DATASOURCE

    // Constructor injection for the DataSource
    public AdminController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/admin")
    public String admin(HttpServletRequest request, Model model) {
        boolean isAdminFromDb = false; // <<< THIS WILL BE THE TRUE SOURCE OF TRUTH
        String usernameFromCookie = "Guest"; // Initialize with a default

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("sessionData".equals(cookie.getName())) {
                    try {
                        byte[] data = Base64.getDecoder().decode(cookie.getValue());
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        ObjectInputStream ois = new ObjectInputStream(bis);

                        // Remediation 1: Deserialization Filter (from previous step)
                        // This filter allows only UserSession objects
                        ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                                "com.example.demo.models.UserSession;" +
                                        "!*; "
                        );
                        ois.setObjectInputFilter(filter);

                        UserSession userSessionFromCookie = (UserSession) ois.readObject(); // Deserialize the object
                        ois.close();

                        usernameFromCookie = userSessionFromCookie.getUsername(); // Get username from deserialized object

                        // !!! REMEDIATION 2: VALIDATE isAdmin AGAINST DATABASE USING JDBC !!!
                        // This is the deep remediation step without JPA.
                        // We use a PreparedStatement to prevent SQL injection here.
                        String sql = "SELECT is_admin FROM user_auth_info WHERE username = ?";
                        try (Connection conn = dataSource.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(sql)) {

                            pstmt.setString(1, usernameFromCookie); // Set the username parameter
                            ResultSet rs = pstmt.executeQuery();

                            if (rs.next()) {
                                // If user found in DB, use DB's isAdmin status as the source of truth
                                isAdminFromDb = rs.getBoolean("is_admin");
                                System.out.println("Validated user '" + usernameFromCookie + "' against DB. isAdmin: " + isAdminFromDb);

                                // Optional: Log if cookie's isAdmin was manipulated
                                if (userSessionFromCookie.isAdmin() != isAdminFromDb) {
                                    System.out.println("SECURITY ALERT: UserSession cookie for '" + usernameFromCookie + "' had isAdmin=" + userSessionFromCookie.isAdmin() + ", but DB says isAdmin=" + isAdminFromDb + ". Potential deserialization attack!");
                                }
                            } else {
                                // User not found in DB for validation, treat as non-admin
                                System.out.println("User '" + usernameFromCookie + "' from cookie not found in DB for validation.");
                                isAdminFromDb = false;
                            }
                        } // Connection, PreparedStatement, ResultSet are auto-closed here

                    } catch (java.io.InvalidClassException ice) {
                        System.err.println("Deserialization blocked by filter (InvalidClassException): " + ice.getMessage());
                        // If blocked by filter, username should remain default or handled as untrusted
                        usernameFromCookie = "Untrusted_User"; // Indicate filtering
                    } catch (Exception e) {
                        System.err.println("Error during deserialization or DB lookup for sessionData cookie: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                        // General deserialization error (e.g., StreamCorruptedException) or DB error, treat as non-admin
                    }
                }
            }
        }

        // Set model attributes based on DB validation result
        model.addAttribute("user", usernameFromCookie); // Display the username from cookie (or default/untrusted)
        model.addAttribute("isAdmin", isAdminFromDb); // This is the final, validated isAdmin status

        // Set message for the view
        if (!isAdminFromDb) {
            model.addAttribute("message", "Access Denied: You must be an administrator to view this page.");
        } else {
            model.addAttribute("message", "Welcome, Administrator!");
        }

        return "admin"; // Always return the admin view
    }
}