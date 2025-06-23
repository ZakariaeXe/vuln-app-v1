package com.example.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.io.IOException;
import com.example.demo.models.UserSession;


@Controller
public class DeserializationController {

    private static final String DEMO_SESSION_KEY = "deserializationDemoUserSession";

    @GetMapping("/deserialize")
    public String showDeserializationForm(HttpSession session, Model model) {
        UserSession currentUserSession = (UserSession) session.getAttribute(DEMO_SESSION_KEY);


        if (currentUserSession == null) {
            currentUserSession = new UserSession("demoUser", false); // Default to non-admin
            session.setAttribute(DEMO_SESSION_KEY, currentUserSession); // Store in the demo-specific key
            System.out.println("No UserSession found for demo, setting a default non-admin demoUserSession.");
        }

        model.addAttribute("currentAdminStatus", currentUserSession.isAdmin() ? "You are currently an ADMIN." : "You are currently a REGULAR user.");
        model.addAttribute("result", "Paste a Base64 encoded serialized UserSession object here to see its effect.");
        return "deserialize-demo"; // Name of your Thymeleaf view
    }

    @PostMapping("/deserialize-action")
    public String doDeserialization(
            @RequestParam("serializedData") String serializedData,
            HttpSession session,
            Model model
    ) {
        String resultMessage = "";
        byte[] decodedBytes;
        Object deserializedObject = null;

        try {

            decodedBytes = Base64.getDecoder().decode(serializedData);
            System.out.println("Payload interpreted as Base64 from form input.");

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", "Invalid Base64 format. Please provide a valid Base64 encoded payload.");
            model.addAttribute("serializedDataInput", serializedData); // Keep input for user convenience
            return "deserialize-demo";
        }

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);

            ObjectInputStream ois = new ObjectInputStream(bis);
            deserializedObject = ois.readObject();

            ois.close();
            bis.close();

            if (deserializedObject instanceof UserSession) {

                UserSession deserializedUserSession = (UserSession) deserializedObject;
                session.setAttribute(DEMO_SESSION_KEY, deserializedUserSession);
                resultMessage = "Deserialization successful. UserSession object modified in session. New isAdmin status: " + deserializedUserSession.isAdmin();
            } else {
                resultMessage = "Deserialization failed: Expected UserSession but received " + deserializedObject.getClass().getName() + ".";
                resultMessage += " Value (toString) : " + deserializedObject.toString();
            }

            model.addAttribute("result", resultMessage);

        } catch (ClassNotFoundException e) {
            model.addAttribute("error", "Deserialization failed: Class not found. " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            model.addAttribute("error", "Deserialization failed (IO Error): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            model.addAttribute("error", "Deserialization failed: An unexpected error occurred. " + e.getMessage());
            e.printStackTrace();
        }

        model.addAttribute("serializedDataInput", serializedData);

        UserSession updatedUserSession = (UserSession) session.getAttribute(DEMO_SESSION_KEY);
        if (updatedUserSession != null) {
            model.addAttribute("currentAdminStatus", updatedUserSession.isAdmin() ? "You are currently an ADMIN." : "You are currently a REGULAR user.");
        }
        return "deserialize-demo";
    }
}