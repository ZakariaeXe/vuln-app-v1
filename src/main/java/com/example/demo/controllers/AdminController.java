package com.example.demo.controllers;

import com.example.demo.models.UserSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;
import java.io.*;
import java.util.Base64;

//@Controller
//public class AdminController {
//
//    @GetMapping("/admin")
//    public String admin(HttpServletRequest request, Model model) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if ("sessionData".equals(cookie.getName())) {
//                    try {
//                        // Désérialisation vulnérable sans validation
//                        byte[] data = Base64.getDecoder().decode(cookie.getValue());
//                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
//                        UserSession userSession = (UserSession) ois.readObject();
//                        ois.close();
//
//                        // Bypass de la vérification si isAdmin est modifié
//                        if (userSession.isAdmin()) {
//                            model.addAttribute("user", userSession.getUsername());
//                            model.addAttribute("isAdmin", true);  // Ajoute la variable isAdmin à la session
//                            return "admin";  // Accès admin
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        return "redirect:/";  // Accès refusé ou cookie mal formé
//    }
//}
@Controller
public class AdminController {

    @GetMapping("/admin")
    public String admin(HttpServletRequest request, Model model) {
        boolean isAdminFromCookie = false;
        String usernameFromCookie = "Guest"; // Default username if not determined

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("sessionData".equals(cookie.getName())) { // Look for the "sessionData" cookie
                    try {
                        // !!! THE VULNERABLE DESERIALIZATION !!!
                        // Deserialization of untrusted data from the cookie without validation
                        byte[] data = Base64.getDecoder().decode(cookie.getValue());
                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                        UserSession userSession = (UserSession) ois.readObject(); // Deserialization occurs here!
                        ois.close();

                        // Access control bypass if isAdmin is manipulated in the deserialized object
                        if (userSession.isAdmin()) { // Trusting the deserialized isAdmin flag
                            isAdminFromCookie = true;
                            usernameFromCookie = userSession.getUsername();
                            System.out.println("Admin access granted for user: " + userSession.getUsername() + " via deserialized cookie.");
                        } else {
                            System.out.println("User '" + userSession.getUsername() + "' is not an admin based on deserialized cookie.");
                        }
                    } catch (Exception e) {
                        System.err.println("Error deserializing sessionData cookie: " + e.getMessage());
                        // If deserialization fails (e.g., malformed cookie), assume not admin and log
                        isAdminFromCookie = false; // Ensure access is denied on error
                    }
                }
            }
        }

        // Add attributes to the model, regardless of admin status
        model.addAttribute("user", usernameFromCookie); // Username from cookie (or "Guest")
        model.addAttribute("isAdmin", isAdminFromCookie); // Admin status determined from cookie

        // Set an error message if not allowed
        if (!isAdminFromCookie) {
            model.addAttribute("message", "Access Denied: You must be an administrator to view this page.");
        } else {
            // Set a welcome message if admin
            model.addAttribute("message", "Welcome, Administrator!");
        }

        return "admin"; // Always return the admin view
    }
}
