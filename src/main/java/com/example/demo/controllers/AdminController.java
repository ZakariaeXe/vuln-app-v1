package com.example.demo.controllers;

import com.example.demo.models.UserSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.*;
import java.io.*;
import java.util.Base64;


@Controller
public class AdminController {

    @GetMapping("/admin")
    public String admin(HttpServletRequest request, Model model) {
        boolean isAdminFromCookie = false;
        String usernameFromCookie = "Guest";

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("sessionData".equals(cookie.getName())) {
                    try {
                        byte[] data = Base64.getDecoder().decode(cookie.getValue());
                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                        UserSession userSession = (UserSession) ois.readObject();
                        ois.close();

                        if (userSession.isAdmin()) {
                            isAdminFromCookie = true;
                            usernameFromCookie = userSession.getUsername();
                            System.out.println("Admin access granted for user: " + userSession.getUsername() + " via deserialized cookie.");
                        } else {
                            System.out.println("User '" + userSession.getUsername() + "' is not an admin based on deserialized cookie.");
                        }
                    } catch (Exception e) {
                        System.err.println("Error deserializing sessionData cookie: " + e.getMessage());

                        isAdminFromCookie = false;
                    }
                }
            }
        }
#just triggering codeql 

        model.addAttribute("user", usernameFromCookie);
        model.addAttribute("isAdmin", isAdminFromCookie);


        if (!isAdminFromCookie) {
            model.addAttribute("message", "Access Denied: You must be an administrator to view this page.");
        } else {

            model.addAttribute("message", "Welcome, Administrator!");
        }

        return "admin";
    }
}
