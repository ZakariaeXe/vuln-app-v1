package com.example.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientSideController {

    @GetMapping("/prototype-pollution")
    public String showPrototypePollutionDemo(HttpSession session, Model model) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("result", "Enter JSON data to demonstrate Prototype Pollution.");
        return "prototype-pollution";
    }
}