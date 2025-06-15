package com.example.demo.controllers;

import com.example.demo.models.Photo;
import com.example.demo.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/search")
    public String search(
            @RequestParam(name = "query", required = false) String q,
            Model model,
            HttpSession session
    ) {
        // Authentication check
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Photo> res = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            // Parameterized query with placeholders for owner_id and the search pattern
            String sql =
                    "SELECT id, filename, description, url, owner_id " +
                            "FROM photo " +
                            "WHERE owner_id = ? AND filename LIKE ?";

            try (Connection c = dataSource.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                // 1) bind the owner_id
                ps.setLong(1, currentUser.getId());
                // 2) bind the search term (with wildcards)
                ps.setString(2, "%" + q + "%");

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Photo p = new Photo();
                        p.setId(rs.getLong("id"));
                        p.setOwnerId(rs.getLong("owner_id"));
                        p.setFilename(rs.getString("filename"));
                        p.setDescription(rs.getString("description"));
                        p.setUrl(rs.getString("url"));
                        res.add(p);
                    }
                }

            } catch (SQLException e) {
                model.addAttribute("error", "Search error: " + e.getMessage());
            }
        }

        model.addAttribute("query", q);
        model.addAttribute("results", res);
        return "search";
    }

//    // Classic SQLi variant
//    @GetMapping("/search")
//    public String search(
//            @RequestParam(name = "query", required = false) String q,
//            Model model,
//            HttpSession session
//    ) {
//        if (session.getAttribute("user") == null) return "redirect:/login";
//        User currentUser = (User) session.getAttribute("user");
//
//        List<Photo> res = new ArrayList<>();
//        if (q != null && !q.isBlank()) {
//            String sql = "SELECT id, filename, description, url, owner_id FROM photo WHERE owner_id = "
//                    + currentUser.getId() + " AND filename LIKE '%" + q + "%'";
//
//            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
//                while (rs.next()) {
//                    Photo p = new Photo();
//                    p.setId(rs.getLong("id"));
//                    p.setOwnerId(rs.getLong("owner_id"));
//                    p.setFilename(rs.getString("filename"));
//                    p.setDescription(rs.getString("description"));
//                    p.setUrl(rs.getString("url"));
//                    res.add(p);
//                }
//            } catch (SQLException e) {
//                model.addAttribute("error", "Search error: " + e.getMessage());
//            }
//        }
//        model.addAttribute("query", q);
//        model.addAttribute("results", res);
//        return "search";
//    }



}

