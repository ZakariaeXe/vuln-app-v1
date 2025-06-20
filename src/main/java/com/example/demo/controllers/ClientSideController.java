package com.example.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientSideController {

    @GetMapping("/prototype-pollution")
    public String showPrototypePollutionDemo(HttpSession session, Model model) {
        // Basic check if a user is "logged in" for consistency
        if (session.getAttribute("user") == null) {
            return "redirect:/login"; // Redirect if not authenticated
        }
        model.addAttribute("result", "Enter JSON data to demonstrate Prototype Pollution.");
        return "prototype-pollution"; // Name of the Thymeleaf view
    }
}