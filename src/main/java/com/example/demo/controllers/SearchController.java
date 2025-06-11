package com.example.demo.controllers;


import com.example.demo.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource; // Use javax.sql for DataSource
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private DataSource dataSource; // Inject DataSource for raw JDBC operations

    @GetMapping("/search")
    public String search(@RequestParam(name = "query", required = false) String query,
                         Model model, HttpSession session) {

        // Basic authentication check
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<User> searchResults = new ArrayList<>();
        if (query != null && !query.trim().isEmpty()) {
            // SQL Injection Vulnerability: Direct string concatenation of 'query' into SQL
            String sql = "SELECT id, username, email, password FROM user WHERE username LIKE '%" + query + "%'";

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement(); // Using Statement instead of PreparedStatement
                 ResultSet resultSet = statement.executeQuery(sql)) {

                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getLong("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    user.setPassword(resultSet.getString("password")); // Fetching password for demonstration (highly insecure!)
                    searchResults.add(user);
                }
            } catch (SQLException e) {
                model.addAttribute("error", "Database error: " + e.getMessage());
                // In a real app, log the full exception: e.printStackTrace();
            }
        }
        model.addAttribute("query", query);
        model.addAttribute("searchResults", searchResults);
        return "search";
    }
}
