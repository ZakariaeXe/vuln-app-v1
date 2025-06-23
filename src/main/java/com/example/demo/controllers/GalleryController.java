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
public class GalleryController {
    @Autowired
    private DataSource dataSource;

    @GetMapping("/gallery")
    public String listPhotos(
            @RequestParam("userId") Long userId,
            Model model,
            HttpSession session
    ) {
        User cur = (User) session.getAttribute("user");
        if (cur == null) return "redirect:/login";

        String sql = "SELECT id,owner_id,filename,description,url FROM photo WHERE owner_id=" + userId;
        List<Photo> photos = new ArrayList<>();
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                Photo p = new Photo();
                p.setId(rs.getLong("id"));
                p.setOwnerId(rs.getLong("owner_id"));
                p.setFilename(rs.getString("filename"));
                p.setDescription(rs.getString("description"));
                p.setUrl(rs.getString("url"));
                photos.add(p);
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Gallery error: " + e.getMessage());
        }
        model.addAttribute("photos", photos);
        model.addAttribute("ownerId", userId);
        return "gallery";
    }

    @GetMapping("/photo/{id}")
    public String viewPhoto(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {
        User cur = (User) session.getAttribute("user");
        if (cur == null) return "redirect:/login";

        String sql = "SELECT id,owner_id,filename,description,url FROM photo WHERE id=" + id;
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                Photo p = new Photo();
                p.setId(rs.getLong("id"));
                p.setOwnerId(rs.getLong("owner_id"));
                p.setFilename(rs.getString("filename"));
                p.setDescription(rs.getString("description"));
                p.setUrl(rs.getString("url"));
                model.addAttribute("photo", p);
                return "photo";
            }
        } catch (SQLException e) {
            model.addAttribute("error", "Photo error: " + e.getMessage());
        }
        return "redirect:/gallery?userId=" + cur.getId();
    }
}