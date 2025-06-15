//package com.example.demo.controllers;
//
//import com.example.demo.models.User;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.*;
//import java.net.*;
//import java.util.stream.Collectors;
//
//@Controller
//public class FetchController {
//    @GetMapping("/fetch")
//    public String showForm(HttpSession session, Model model) {
//        if (session.getAttribute("user") == null) return "redirect:/login";
//        return "fetch";
//    }
//
//    @GetMapping("/fetch-action")
//    public String doFetch(
//            @RequestParam String url,
//            HttpSession session,
//            Model model
//    ) {
//        if (session.getAttribute("user") == null) return "redirect:/login";
//        StringBuilder buf = new StringBuilder();
//        try {
//            // SSRF: no URL validation
//            URL u = new URL(url);
//            URLConnection cn = u.openConnection();
//            cn.setConnectTimeout(5000);
//            try (BufferedReader r = new BufferedReader(new InputStreamReader(cn.getInputStream()))) {
//                buf.append(r.lines().collect(Collectors.joining("\n")));
//            }
//            model.addAttribute("result", buf.toString().replace("<","&lt;").replace(">","&gt;"));
//        } catch (Exception e) {
//            model.addAttribute("error", "Fetch error: " + e.getMessage());
//        }
//        model.addAttribute("url", url);
//        return "fetch";
//    }
//}

package com.example.demo.controllers;

import com.example.demo.models.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;   // Pour les en-têtes HTTP
import org.springframework.http.HttpStatus;   // Pour les codes de statut HTTP
import org.springframework.http.MediaType;    // Pour les types de média (image/jpeg, etc.)
import org.springframework.http.ResponseEntity; // Pour renvoyer des données binaires avec en-têtes
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;

@Controller
public class FetchController {
    @GetMapping("/fetch")
    public String showForm(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        return "fetch"; // Cette vue affichera les deux formulaires
    }

    @GetMapping("/fetch-action")
    public String fetchUrlContent(
            @RequestParam String url,
            HttpSession session,
            Model model) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        String result = "";
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            // Allow only Medium domains
            if (host == null || !host.endsWith("medium.com")) {
                throw new IllegalArgumentException("URL not allowed: " + host);
            }
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Escape HTML to prevent XSS in fetched content
            result = response.body().replace("<", "&lt;").replace(">", "&gt;");
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching URL: " + e.getMessage());
        }
        model.addAttribute("url", url);
        model.addAttribute("result", result);
        return "fetch";
    }

    @GetMapping("/fetch-image")
    @ResponseBody
    public ResponseEntity<byte[]> fetchImage(
            @RequestParam("imageUrl") String imageUrl,
            HttpSession session) {
        if (session.getAttribute("user") == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            URI uri = new URI(imageUrl);
            String host = uri.getHost();
            // Allow only Google image hosts
            if (host == null ||
                    !(host.endsWith("google.com") || host.endsWith("gstatic.com"))) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("application/octet-stream");
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(response.body());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}