package com.example.demo.controllers;

import com.example.demo.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

@Controller
public class UrlFetcherController {

    @GetMapping("/fetch-url")
    public String showUrlFetcherForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login"; // Must be logged in to access
        }
        model.addAttribute("result", "Enter a URL to fetch its content.");
        return "url-fetcher";
    }

    @GetMapping("/fetch-url-action") // Separate endpoint for processing to keep form clean
    public String fetchUrl(@RequestParam("url") String urlString, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login"; // Must be logged in to access
        }

        StringBuilder content = new StringBuilder();
        try {
            // SSRF Vulnerability: No validation or sanitization of the URL.
            // The application will attempt to fetch content from any URL provided.
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection(); // Opening connection to user-supplied URL

            // Set a timeout to prevent hanging
            conn.setConnectTimeout(5000); // 5 seconds
            conn.setReadTimeout(5000);   // 5 seconds

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                content.append(reader.lines().collect(Collectors.joining("\n")));
            }
            model.addAttribute("result", "Fetched Content:<br><pre>" + content.toString().replace("<", "&lt;").replace(">", "&gt;") + "</pre>");

        } catch (Exception e) {
            model.addAttribute("error", "Error fetching URL: " + e.getMessage());
            e.printStackTrace();
        }
        model.addAttribute("submittedUrl", urlString); // Keep the URL in the form
        return "url-fetcher";
    }
}