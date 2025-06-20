package com.example.demo.controllers;
import com.example.demo.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

@Controller
public class UploadController {
    @Autowired
    private DataSource dataSource;

    @GetMapping("/upload")
    public String showUpload(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String desc,
            @RequestParam(value = "url", required = false) String url,
            HttpSession session,
            Model model) {
        User u = (User) session.getAttribute("user");
        if (u == null) return "redirect:/login";

        String filename = file.getOriginalFilename();
        String sql = "INSERT INTO photo (owner_id, filename, description, url) VALUES (?, ?, ?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, u.getId());
            ps.setString(2, filename);
            ps.setString(3, desc);
            ps.setString(4, url != null ? url : "");
            ps.executeUpdate();

        } catch (SQLException e) {
            model.addAttribute("error", "Upload error: " + e.getMessage());
            return "upload";
        }
        return "redirect:/gallery?userId=" + u.getId();
    }


    @PostMapping(value="/upload-xml", consumes="application/xml")
    public String uploadXml(
            @RequestBody String xml,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        StringBuilder out = new StringBuilder();


        try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();


            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://xml.org/sax/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder db = dbf.newDocumentBuilder();

            db.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));

            Document doc = db.parse(new InputSource(new StringReader(xml)));

            Node rootElement = doc.getDocumentElement();
            if (rootElement != null) {

                String extractedContent = rootElement.getTextContent();

                if (extractedContent.trim().isEmpty()) {
                    out.append("Processing successful. XML document parsed, but no extractable text content found from root element.");
                } else {
                    out.append("Extracted content from XML: ").append(extractedContent).append("<br>");
                }
            } else {
                out.append("XML document parsed, but no root element found for extraction.");
            }

        } catch (Exception e) {
            out.append("XML parsing error: ").append(e.getMessage()).append("<br>");
            out.append("Submitted XML (for debug): ").append(xml.replace("<", "&lt;").replace(">", "&gt;"));
        }

        model.addAttribute("result", out.toString());

        return "upload";
    }
}