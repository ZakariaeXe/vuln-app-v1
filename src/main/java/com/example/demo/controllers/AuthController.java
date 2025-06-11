package com.example.demo.controllers;

import org.springframework.ui.Model;
import com.example.demo.models.User;
import com.example.demo.repos.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource; // Import DataSource
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Import Statement
@Controller
public class AuthController {

    @Autowired
//    private UserRepository userRepository;
    private DataSource dataSource; // Inject DataSource for raw JDBC operations


    @GetMapping(value = "/")
    public String homePage(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            // Logged in user
            model.addAttribute("loggedIn", true);
            model.addAttribute("username", currentUser.getUsername());
            model.addAttribute("userId", currentUser.getId());
            model.addAttribute("welcomeMessage", "Welcome back, " + currentUser.getUsername() + "!");
            model.addAttribute("dashboardInfo", "Your personalized dashboard content and recent activity would go here.");
            model.addAttribute("securityTip", "Remember to always use unique and strong passwords for your accounts!");
        } else {
            // Not logged in
            model.addAttribute("loggedIn", false);
            model.addAttribute("welcomeMessage", "Welcome to the Vulnerable App!");
            model.addAttribute("callToAction", "This application is intentionally built with security flaws for educational purposes. Please Login or Register to explore the functionalities and vulnerabilities.");
        }
        return "home"; // This will render home.html
    }

//    @GetMapping("/")
//    public String defaultPage() {
//        return "redirect:/home";
//    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        String insertSql = "INSERT INTO user (username, password, email) VALUES ('" + user.getUsername() + "', '" + user.getPassword() + "', '" + user.getEmail() + "')";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(insertSql);
        } catch (SQLException e) {
            // Log error or add to model
            e.printStackTrace();
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
        return "redirect:/login";
    }

//    @PostMapping("/register")
//    public String registerUser(@ModelAttribute User user) {
//        userRepository.save(user);  // No password hashing for now → vulnerable
//        return "redirect:/login";
//    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }


    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session, Model model) {

        User user = null; // We will populate this manually

        // --- SQL Injection Vulnerability during Login ---
        // This query directly concatenates username and password.
        // It's highly vulnerable to authentication bypass.
        String sql = "SELECT id, username, password, email FROM user WHERE username = '" + username + "' AND password = '" + password + "'";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement(); // Using Statement, not PreparedStatement
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                // If a row is returned, credentials are "valid"
                user = new User();
                user.setId(resultSet.getLong("id"));
                user.setUsername(resultSet.getString("username"));
                user.setPassword(resultSet.getString("password")); // Storing plain text password for demo
                user.setEmail(resultSet.getString("email"));

                session.setAttribute("user", user);
                return "redirect:/profile/" + user.getId();
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Database error during login: " + e.getMessage());
            e.printStackTrace(); // For debugging
            return "login";
        }
    }
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return "redirect:/login";
    }


    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        User profileUser = null;
        // Manual JDBC fetch for profile page
        String sql = "SELECT id, username, password, email FROM user WHERE id = " + id; // IDOR vulnerable here too if ID is not sanitized

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                profileUser = new User();
                profileUser.setId(resultSet.getLong("id"));
                profileUser.setUsername(resultSet.getString("username"));
                profileUser.setPassword(resultSet.getString("password"));
                profileUser.setEmail(resultSet.getString("email"));
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Database error fetching profile: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/profile/" + currentUser.getId(); // Redirect back to their own profile
        }


        if (profileUser != null) {
            model.addAttribute("profileUser", profileUser);
            model.addAttribute("criticalInfo", "User " + profileUser.getUsername() + "'s top secret project details!");
            return "profile";
        } else {
            model.addAttribute("error", "User not found.");
            return "redirect:/profile/" + currentUser.getId();
        }
    }
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
}
