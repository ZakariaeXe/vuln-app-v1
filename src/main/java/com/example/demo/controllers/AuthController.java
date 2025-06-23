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
    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model,
            HttpServletResponse response
    ) {

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
                session.setAttribute("user", u);


                UserSession userSession = new UserSession(u.getUsername(), false);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(userSession);
                oos.close();
                String cookieValue = Base64.getEncoder().encodeToString(baos.toByteArray());


                Cookie cookie = new Cookie("sessionData", cookieValue);
                cookie.setPath("/");
                response.addCookie(cookie);


                return "redirect:/";
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
        } catch (SQLException | IOException e) {
            model.addAttribute("error", "Login error: " + e.getMessage());
            return "login";
        }
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
