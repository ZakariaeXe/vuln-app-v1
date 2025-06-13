package com.example.demo.controllers;
import com.example.demo.models.Photo;
import com.example.demo.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class GalleryController {
    @Autowired
    private DataSource dataSource;

    @GetMapping("/gallery")
    public String listPhotos(@RequestParam Long userId, HttpSession session, Model model) {
        User cur = (User) session.getAttribute("user");
        if (cur == null) return "redirect:/login";
        if (!cur.getId().equals(userId)) {
            throw new AccessDeniedException("Cannot view another user's gallery");
        }
        String sql = "SELECT id,filename,description,url,owner_id FROM photo WHERE owner_id = ?";
        List<Photo> photos = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Photo p = new Photo();
                    p.setId(rs.getLong("id"));
                    p.setFilename(rs.getString("filename"));
                    p.setDescription(rs.getString("description"));
                    p.setUrl(rs.getString("url"));
                    p.setOwnerId(rs.getLong("owner_id"));
                    photos.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("photos", photos);
        model.addAttribute("ownerId", userId);
        return "gallery";
    }

    @GetMapping("/photo/{id}")
    public String viewPhoto(@PathVariable Long id, Model model, HttpSession session) {
        User cur = (User) session.getAttribute("user");
        if (cur == null) return "redirect:/login";

        String sql = "SELECT id, filename, description, url, owner_id FROM photo WHERE id = ?";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long ownerId = rs.getLong("owner_id");
                    if (!cur.getId().equals(ownerId)) {
                        throw new AccessDeniedException("Cannot view another user's photo");
                    }
                    Photo p = new Photo();
                    p.setId(id);
                    p.setFilename(rs.getString("filename"));
                    p.setDescription(rs.getString("description"));
                    p.setUrl(rs.getString("url"));
                    p.setOwnerId(ownerId);
                    model.addAttribute("photo", p);
                    return "photo";
                }
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Photo error: " + e.getMessage());
        }
        return "redirect:/gallery?userId=" + cur.getId();
    }


//    @GetMapping("/gallery")
//    public String listPhotos(
//            @RequestParam("userId") Long userId,
//            Model model,
//            HttpSession session
//    ) {
//        User cur = (User) session.getAttribute("user");
//        if (cur == null) return "redirect:/login";
//
//        // IDOR: no check that userId==cur.getId()
//        String sql = "SELECT id,owner_id,filename,description,url FROM photo WHERE owner_id=" + userId;
//        List<Photo> photos = new ArrayList<>();
//        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
//            while (rs.next()) {
//                Photo p = new Photo();
//                p.setId(rs.getLong("id"));
//                p.setOwnerId(rs.getLong("owner_id"));
//                p.setFilename(rs.getString("filename"));
//                p.setDescription(rs.getString("description"));
//                p.setUrl(rs.getString("url"));
//                photos.add(p);
//            }
//        } catch (SQLException e) {
//            model.addAttribute("error", "Gallery error: " + e.getMessage());
//        }
//        model.addAttribute("photos", photos);
//        model.addAttribute("ownerId", userId);
//        return "gallery"; // Thymeleaf will render descriptions unescaped via th:utext for XSS
//    }

//    @GetMapping("/photo/{id}")
//    public String viewPhoto(
//            @PathVariable Long id,
//            Model model,
//            HttpSession session
//    ) {
//        User cur = (User) session.getAttribute("user");
//        if (cur == null) return "redirect:/login";
//
//        String sql = "SELECT id,owner_id,filename,description,url FROM photo WHERE id=" + id;
//        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
//            if (rs.next()) {
//                Photo p = new Photo();
//                p.setId(rs.getLong("id"));
//                p.setOwnerId(rs.getLong("owner_id"));
//                p.setFilename(rs.getString("filename"));
//                p.setDescription(rs.getString("description"));
//                p.setUrl(rs.getString("url"));
//                model.addAttribute("photo", p);
//                return "photo";
//            }
//        } catch (SQLException e) {
//            model.addAttribute("error", "Photo error: " + e.getMessage());
//        }
//        return "redirect:/gallery?userId=" + cur.getId();
//    }
}