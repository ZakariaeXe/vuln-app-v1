package com.example.demo.controllers;

import com.example.demo.models.User; // Pour la vérification de session
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable; // Nécessaire si vous désérialisez vers une interface/classe non spécifique
import java.util.Base64; // Pour le décodage Base64

import java.io.IOException;



import com.example.demo.models.UserSession; // Import your UserSession class


@Controller
public class DeserializationController {

    // Using a distinct session attribute key to avoid conflicts with other 'User' objects
    private static final String DEMO_SESSION_KEY = "deserializationDemoUserSession";

    // Handles displaying the deserialization form and current user status
    @GetMapping("/deserialize")
    public String showDeserializationForm(HttpSession session, Model model) {
        // Retrieve the demo-specific UserSession from the session
        UserSession currentUserSession = (UserSession) session.getAttribute(DEMO_SESSION_KEY);

        // --- TEST SETUP: Ensure a UserSession is in session for demo purposes ---
        // If no demo session exists (first visit or after a full logout/restart), create a default non-admin one.
        // This ensures the demo always has a UserSession object to display and manipulate.
        if (currentUserSession == null) {
            currentUserSession = new UserSession("demoUser", false); // Default to non-admin
            session.setAttribute(DEMO_SESSION_KEY, currentUserSession); // Store in the demo-specific key
            System.out.println("No UserSession found for demo, setting a default non-admin demoUserSession.");
        }
        // --- END TEST SETUP ---


        // Add the current user's admin status to the model for display in the view
        model.addAttribute("currentAdminStatus", currentUserSession.isAdmin() ? "You are currently an ADMIN." : "You are currently a REGULAR user.");
        model.addAttribute("result", "Paste a Base64 encoded serialized UserSession object here to see its effect.");
        return "deserialize-demo"; // Name of your Thymeleaf view
    }

    // Handles the submission of the deserialization form
    @PostMapping("/deserialize-action")
    public String doDeserialization(
            @RequestParam("serializedData") String serializedData, // User-provided Base64 string
            HttpSession session,
            Model model
    ) {
        String resultMessage = "";
        byte[] decodedBytes;
        Object deserializedObject = null;

        try {
            // Step 1: Decode the Base64 string provided by the user
            decodedBytes = Base64.getDecoder().decode(serializedData);
            System.out.println("Payload interpreted as Base64 from form input.");

        } catch (IllegalArgumentException e) {
            // Handle cases where the input is not valid Base64
            model.addAttribute("error", "Invalid Base64 format. Please provide a valid Base64 encoded payload.");
            model.addAttribute("serializedDataInput", serializedData); // Keep input for user convenience
            return "deserialize-demo";
        }

        try {
            // Step 2: Create an ObjectInputStream from the decoded bytes
            ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);

            // !!! THE VULNERABLE LINE !!!
            // This reads an arbitrary object from the input stream.
            // There is no validation or filtering of the object's type or content.
            ObjectInputStream ois = new ObjectInputStream(bis);
            deserializedObject = ois.readObject(); // Deserialization occurs here!

            // Close the streams
            ois.close();
            bis.close();

            // Step 3: Demonstrate the exploitation impact
            if (deserializedObject instanceof UserSession) {
                // If the deserialized object is a UserSession, replace the current session object.
                // This is the core of the privilege escalation vulnerability:
                // An attacker can inject a UserSession object with isAdmin=true.
                UserSession deserializedUserSession = (UserSession) deserializedObject;
                session.setAttribute(DEMO_SESSION_KEY, deserializedUserSession); // Overwriting session 'deserializationDemoUserSession' with untrusted data
                resultMessage = "Deserialization successful. UserSession object modified in session. New isAdmin status: " + deserializedUserSession.isAdmin();
            } else {
                // Handle cases where a different type of object was deserialized (for broader testing)
                resultMessage = "Deserialization failed: Expected UserSession but received " + deserializedObject.getClass().getName() + ".";
                resultMessage += " Value (toString) : " + deserializedObject.toString();
            }

            model.addAttribute("result", resultMessage);

        } catch (ClassNotFoundException e) {
            // Handle cases where a class specified in the payload is not found on the application's classpath
            model.addAttribute("error", "Deserialization failed: Class not found. " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            // Handle general IO errors during deserialization (e.g., corrupted stream format)
            model.addAttribute("error", "Deserialization failed (IO Error): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            // Catch any other unexpected exceptions (e.g., from gadget chains if present)
            model.addAttribute("error", "Deserialization failed: An unexpected error occurred. " + e.getMessage());
            e.printStackTrace();
        }

        model.addAttribute("serializedDataInput", serializedData); // Keep the payload in the textarea for review

        // Update the displayed admin status after potential modification
        UserSession updatedUserSession = (UserSession) session.getAttribute(DEMO_SESSION_KEY);
        if (updatedUserSession != null) {
            model.addAttribute("currentAdminStatus", updatedUserSession.isAdmin() ? "You are currently an ADMIN." : "You are currently a REGULAR user.");
        }
        return "deserialize-demo"; // Return to the same view
    }
}