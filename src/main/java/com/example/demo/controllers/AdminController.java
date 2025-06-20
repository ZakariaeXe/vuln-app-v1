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

    private final DataSource dataSource;


    public AdminController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/admin")
    public String admin(HttpServletRequest request, Model model) {
        boolean isAdminFromDb = false;
        String usernameFromCookie = "Guest";

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("sessionData".equals(cookie.getName())) {
                    try {
                        byte[] data = Base64.getDecoder().decode(cookie.getValue());
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        ObjectInputStream ois = new ObjectInputStream(bis);


                        ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                                "com.example.demo.models.UserSession;" +
                                        "!*; "
                        );
                        ois.setObjectInputFilter(filter);

                        UserSession userSessionFromCookie = (UserSession) ois.readObject();
                        ois.close();

                        usernameFromCookie = userSessionFromCookie.getUsername();


                        String sql = "SELECT is_admin FROM user_auth_info WHERE username = ?";
                        try (Connection conn = dataSource.getConnection();
                             PreparedStatement pstmt = conn.prepareStatement(sql)) {

                            pstmt.setString(1, usernameFromCookie);
                            ResultSet rs = pstmt.executeQuery();

                            if (rs.next()) {

                                isAdminFromDb = rs.getBoolean("is_admin");
                                System.out.println("Validated user '" + usernameFromCookie + "' against DB. isAdmin: " + isAdminFromDb);


                                if (userSessionFromCookie.isAdmin() != isAdminFromDb) {
                                    System.out.println("SECURITY ALERT: UserSession cookie for '" + usernameFromCookie + "' had isAdmin=" + userSessionFromCookie.isAdmin() + ", but DB says isAdmin=" + isAdminFromDb + ". Potential deserialization attack!");
                                }
                            } else {

                                System.out.println("User '" + usernameFromCookie + "' from cookie not found in DB for validation.");
                                isAdminFromDb = false;
                            }
                        }

                    } catch (java.io.InvalidClassException ice) {
                        System.err.println("Deserialization blocked by filter (InvalidClassException): " + ice.getMessage());

                        usernameFromCookie = "Untrusted_User";
                    } catch (Exception e) {
                        System.err.println("Error during deserialization or DB lookup for sessionData cookie: " + e.getClass().getSimpleName() + " - " + e.getMessage());

                    }
                }
            }
        }

        model.addAttribute("user", usernameFromCookie);
        model.addAttribute("isAdmin", isAdminFromDb);


        if (!isAdminFromDb) {
            model.addAttribute("message", "Access Denied: You must be an administrator to view this page.");
        } else {
            model.addAttribute("message", "Welcome, Administrator!");
        }

        return "admin";
    }
}