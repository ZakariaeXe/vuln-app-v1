package com.example.demo.controllers;
import com.example.demo.models.User;
import com.example.demo.models.UserSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.Base64;

@Controller
public class AuthController {
    @Autowired
    private DataSource dataSource;

    @Autowired
    private HttpServletResponse response;

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u != null) {
            model.addAttribute("loggedIn", true);
            model.addAttribute("username", u.getUsername());
        } else {
            model.addAttribute("loggedIn", false);
            model.addAttribute("welcome", "Welcome to VulnGallery — Please login or register.");
        }
        return "home";
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        // SQLi: concatenated INSERT
        String sql = "INSERT INTO user (username,password,email) VALUES ('"
                + user.getUsername() + "','"
                + user.getPassword() + "','"
                + user.getEmail() + "')";
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate(sql);
            return "redirect:/login";
        } catch (SQLException e) {
            model.addAttribute("error", "Registration error: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }
    @PostMapping("/login") // This is your existing login endpoint
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model,
            HttpServletResponse response // ADD THIS PARAMETER
    ) {
        // SQLi: concatenated SELECT (vulnerable for another demo, ignore for deserialization)
        String sql = "SELECT id,username,password,email FROM user WHERE username='"
                + username + "' AND password='" + password + "'";
        try (Connection c = dataSource.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setEmail(rs.getString("email"));
                session.setAttribute("user", u); // This sets your primary User object in session

                // --- VULNERABLE SERIALIZATION HERE (TO COOKIE) ---
                UserSession userSession = new UserSession(u.getUsername(), false);  // False by default (non-admin)

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(userSession); // Serialize the UserSession object
                oos.close();
                String cookieValue = Base64.getEncoder().encodeToString(baos.toByteArray());

                // Store the serialized object in a cookie
                Cookie cookie = new Cookie("sessionData", cookieValue);
                cookie.setPath("/"); // Make it available across the application
                response.addCookie(cookie); // Add the cookie to the HTTP response

                // IDOR via profile/{id} (ignore for this demo)
                return "redirect:/"; // Redirect to home or another page
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "login"; // Return to login page on failure
            }
        } catch (SQLException | IOException e) {
            model.addAttribute("error", "Login error: " + e.getMessage());
            return "login";
        }
    }
//    @PostMapping("/login")
//    public String login(
//            @RequestParam String username,
//            @RequestParam String password,
//            HttpSession session,
//            Model model) {
//        // SQLi: concatenated SELECT
//        String sql = "SELECT id,username,password,email FROM user WHERE username='"
//                + username + "' AND password='" + password + "'";
//        try (Connection c = dataSource.getConnection();
//             Statement s = c.createStatement();
//             ResultSet rs = s.executeQuery(sql)) {
//            if (rs.next()) {
//                User u = new User();
//                u.setId(rs.getLong("id"));
//                u.setUsername(rs.getString("username"));
//                u.setPassword(rs.getString("password"));
//                u.setEmail(rs.getString("email"));
//                session.setAttribute("user", u);
//                // Sérialisation vulnérable de l'objet UserSession
//                UserSession userSession = new UserSession(u.getUsername(), false);  // False par défaut (non admin)
//
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                ObjectOutputStream oos = new ObjectOutputStream(baos);
//                oos.writeObject(userSession);
//                oos.close();
//                String cookieValue = Base64.getEncoder().encodeToString(baos.toByteArray());
//
//                // Stocker l'objet sérialisé dans un cookie
//                Cookie cookie = new Cookie("sessionData", cookieValue);
//                cookie.setPath("/");
//                response.addCookie(cookie);
//
//                session.setAttribute("user", u);
//
//                // IDOR via profile/{id}
//                return "redirect:/";
//            } else {
//                model.addAttribute("error", "Invalid credentials");
//                return "login";
//            }
//        } catch (SQLException | IOException e) {
//            model.addAttribute("error", "Login error: " + e.getMessage());
//            return "login";
//        }
//    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

//    @GetMapping("/")
//    public String defaultPage() {
//        return "redirect:/home";
//    }

//    @PostMapping("/register")
//    public String registerUser(@ModelAttribute User user) {
//        userRepository.save(user);  // No password hashing for now → vulnerable
//        return "redirect:/login";
//    }

//    @PostMapping("/login")
//    public String login(@RequestParam String username,
//                        @RequestParam String password,
//                        HttpSession session, Model model) {
//        User user = userRepository.findByUsername(username);
//        if (user != null && user.getPassword().equals(password)) {
//            session.setAttribute("user", user);
//            return "redirect:/profile/"+ user.getId();
//        } else {
//            model.addAttribute("error", "Invalid credentials");
//            return "login";
//        }
//    }


// ... (reste de votre AuthController) ...
//    USING Repository interface: // --- Profile Section (IDOR Vulnerability) ---
//    @GetMapping("/profile/{id}")
//    public String viewProfile(@PathVariable Long id, Model model, HttpSession session) {
//        // Check if user is logged in (basic check)
//        User currentUser = (User) session.getAttribute("user");
//        if (currentUser == null) {
//            // Not logged in, redirect to login
//            return "redirect:/login";
//        }
//
//        // IDOR Vulnerability: Allows any logged-in user to view any profile by changing 'id' in the URL
//        // No authorization check to see if currentUser.getId() == id
//        User profileUser = userRepository.findById(id).orElse(null);
//
//        if (profileUser != null) {
//            model.addAttribute("profileUser", profileUser);
//            // Example of something "critical" (can be anything, just for display)
//            model.addAttribute("criticalInfo", "User " + profileUser.getUsername() + "'s top secret project details!");
//            return "profile";
//        } else {
//            model.addAttribute("error", "User not found.");
//            return "redirect:/profile/" + currentUser.getId(); // Redirect back to their own profile if not found
//        }
//    }
