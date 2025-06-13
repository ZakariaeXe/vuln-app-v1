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

    // Classic SQLi variant
    @GetMapping("/search")
    public String search(
            @RequestParam(name = "query", required = false) String q,
            Model model,
            HttpSession session
    ) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        User currentUser = (User) session.getAttribute("user");

        List<Photo> res = new ArrayList<>();
        if (q != null && !q.isBlank()) {
//            String sql = "SELECT id,owner_id,filename,description,url FROM photo WHERE filename LIKE '%" + q + "%'";
            String sql = "SELECT id, filename, description, url, owner_id FROM photo WHERE owner_id = "
                    + currentUser.getId() + " AND filename LIKE '%" + q + "%'";

            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
                while (rs.next()) {
                    Photo p = new Photo();
                    p.setId(rs.getLong("id"));
                    p.setOwnerId(rs.getLong("owner_id"));
                    p.setFilename(rs.getString("filename"));
                    p.setDescription(rs.getString("description"));
                    p.setUrl(rs.getString("url"));
                    res.add(p);
                }
            } catch (SQLException e) {
                model.addAttribute("error", "Search error: " + e.getMessage());
            }
        }
        model.addAttribute("query", q);
        model.addAttribute("results", res);
        return "search";
    }

}

    // UNION-based SQLi variant
//    @GetMapping("/search-union")
//    public String searchUnion(
//            @RequestParam(name = "query", required = false) String q,
//            Model model,
//            HttpSession session
//    ) {
//        if (session.getAttribute("user") == null) return "redirect:/login";
//
//        List<String> rows = new ArrayList<>();
//        if (q != null && !q.isBlank()) {
//            String sql = "SELECT filename FROM photo WHERE filename LIKE '%" + q + "%' "
//                    + "UNION SELECT username FROM user--";
//            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
//                while (rs.next()) rows.add(rs.getString(1));
//            } catch (SQLException e) {
//                model.addAttribute("error", "Union search error: " + e.getMessage());
//            }
//        }
//        model.addAttribute("query", q);
//        model.addAttribute("rows", rows);
//        return "search-union";
//    }
//}
