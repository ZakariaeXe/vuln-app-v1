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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64; // Make sure this is imported

//@Controller
//public class DeserializationController {
//
//    @GetMapping("/deserialize")
//    public String showDeserializationForm(HttpSession session, Model model) {
//        User currentUser = (User) session.getAttribute("user");
//        if (currentUser == null) return "redirect:/login"; // Must be logged in
//        model.addAttribute("result", "Enter the Java serialized object (raw binary from ysoserial or Base64 encoded). The application will attempt to handle both for learning purposes.");
//        return "deserialize-demo"; // Thymeleaf view name
//    }
//
//    @PostMapping("/deserialize-action")
//    public String doDeserialization(
//            @RequestParam("serializedData") String serializedData, // User-provided serialized data
//            HttpSession session,
//            Model model
//    ) {
//        User currentUser = (User) session.getAttribute("user");
//        if (currentUser == null) return "redirect:/login"; // Must be logged in
//
//        String decodedString = "";
//        byte[] decodedBytes;
//
//        try {
//            // *** MODIFICATION START ***
//            // Attempt to decode as Base64. If it fails, assume it's raw binary and encode it first.
//            try {
//                decodedBytes = Base64.getDecoder().decode(serializedData);
//            } catch (IllegalArgumentException e) {
//                // If direct Base64 decoding fails, assume it's raw binary payload.
//                // Encode it to Base64, then decode. This is for learning simplification.
//                System.out.println("Attempting to interpret input as raw binary, then Base64 encode/decode.");
//                byte[] rawBytes = serializedData.getBytes("ISO-8859-1"); // Use a single-byte encoding
//                String tempBase64Encoded = Base64.getEncoder().encodeToString(rawBytes);
//                decodedBytes = Base64.getDecoder().decode(tempBase64Encoded);
//            }
//            // *** MODIFICATION END ***
//
//            ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
//
//            // THE VULNERABLE LINE: ObjectInputStream.readObject() on untrusted input
//            ObjectInputStream ois = new ObjectInputStream(bis);
//            Object obj = ois.readObject(); // Deserialization happens here!
//
//            ois.close();
//            bis.close();
//
//            // For demonstration, try to display it for feedback
//            decodedString = "Deserialization successful. Object type: " + obj.getClass().getName();
//            decodedString += ", Value (toString): " + obj.toString(); // Display string representation of the object
//
//            model.addAttribute("result", decodedString);
//
//        } catch (ClassNotFoundException e) {
//            model.addAttribute("error", "Deserialization failed: Class not found. " + e.getMessage());
//            e.printStackTrace();
//        } catch (IOException e) { // Catch IOException (e.g., stream issues)
//            model.addAttribute("error", "Deserialization failed (IO Error): " + e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e) { // Catch any other exceptions (e.g., security exceptions from gadget chains)
//            model.addAttribute("error", "Deserialization failed: An unexpected error occurred. " + e.getMessage());
//            e.printStackTrace();
//        }
//        model.addAttribute("serializedDataInput", serializedData); // Keep the payload in the textarea
//        return "deserialize-demo";
//    }
//}


//@Controller
//public class DeserializationController {
//
//    @GetMapping("/deserialize")
//    public String showDeserializationForm(HttpSession session, Model model) {
//        User currentUser = (User) session.getAttribute("user");
//        if (currentUser == null) return "redirect:/login"; // Doit être connecté
//        model.addAttribute("result", "Entrez l'objet Java sérialisé encodé en Base64 ici.");
//        return "deserialize-demo"; // Nom de la vue Thymeleaf
//    }
//
//    @PostMapping("/deserialize-action")
//    public String doDeserialization(
//            @RequestParam("serializedData") String serializedData, // Données sérialisées fournies par l'utilisateur
//            HttpSession session,
//            Model model
//    ) {
//        User currentUser = (User) session.getAttribute("user");
//        if (currentUser == null) return "redirect:/login"; // Doit être connecté
//
//        String decodedString = "";
//        try {
//            // L'application désérialise directement des données non fiables fournies par l'utilisateur.
//            // Si le classpath contient des "chaînes de gadgets" (par ex. Apache Commons Collections),
//            // un attaquant peut obtenir l'exécution de code à distance (RCE).
//
//
////            // Skip Base64 decoding and assume direct binary payload
////            byte[] decodedBytes = serializedData.getBytes();  // Treat the input as direct binary
////
////            ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
////
////            // LIGNE VULNÉRABLE : Désérialisation d'un objet malveillant
////            ObjectInputStream ois = new ObjectInputStream(bis);
////            Object obj = ois.readObject(); // L'objet malveillant est désérialisé ici !
//
//            byte[] decodedBytes = Base64.getDecoder().decode(serializedData);
//            ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
//
//            // LA LIGNE VULNÉRABLE : ObjectInputStream.readObject() sur une entrée non fiable
//            ObjectInputStream ois = new ObjectInputStream(bis);
//            Object obj = ois.readObject(); // La désérialisation se produit ici !
//
//            ois.close();
//            bis.close();
//
//            // Pour la démonstration, on essaie de l'afficher pour un feedback
//            decodedString = "Désérialisation réussie. Type d'objet : " + obj.getClass().getName();
//            // Vous pouvez tenter un cast si vous savez à quel type vous vous attendez, mais l'exploit ne le ferait pas.
//            // Ex: if (obj instanceof UserSession) { decodedString += ", isAdmin: " + ((UserSession)obj).isAdmin(); }
//            decodedString += ", Valeur (toString) : " + obj.toString(); // Afficher la représentation string de l'objet
//
//            model.addAttribute("result", decodedString);
//
//        } catch (ClassNotFoundException e) {
//            model.addAttribute("error", "Échec de la désérialisation : Classe non trouvée. " + e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e) { // Capture IOException, IllegalArgumentException (pour Base64) et autres.
//            model.addAttribute("error", "Échec de la désérialisation : " + e.getMessage());
//            e.printStackTrace();
//        }
//        model.addAttribute("serializedDataInput", serializedData); // Pour garder le payload dans le textarea
//        return "deserialize-demo";
//    }
//}


import org.springframework.core.io.ClassPathResource; // New import
import org.springframework.core.io.Resource; // New import


import java.io.InputStream; // New import for ClassPathResource.getInputStream()


//@Controller
//public class DeserializationController {
//
//    private static final String PAYLOAD_FILE_PATH = "raw-payloadfinal.bin"; // Name of the file in src/main/resources
//
//    @GetMapping("/deserialize")
//    public String showDeserializationForm(HttpSession session, Model model) {
//        User currentUser = (User) session.getAttribute("user");
//        if (currentUser == null) return "redirect:/login";
//        model.addAttribute("result", "Enter the Java serialized object (Base64 encoded) or trigger file deserialization.");
//        return "deserialize-demo";
//    }
//
//    @PostMapping("/deserialize-action")
//    public String doDeserialization(
//            @RequestParam("serializedData") String serializedData,
//            HttpSession session,
//            Model model
//    ) {
//        User currentUser = (User) session.getAttribute("user");
//        if (currentUser == null) return "redirect:/login";
//
//        String resultMessage = "";
//        byte[] decodedBytes;
//
//        try {
//            // Attempt to decode as Base64. This is the "normal" (attacker-provided) way.
//            decodedBytes = Base64.getDecoder().decode(serializedData);
//            System.out.println("Payload interpreted as Base64 from form input.");
//
//        } catch (IllegalArgumentException e) {
//            // If direct Base64 decoding fails, assume it's malformed Base64 or an attempt to paste raw.
//            // For this specific learning scenario, we'll indicate an error for form paste for now.
//            // If you *still* want to allow raw pasting, you'd re-enable the ISO-8859-1 conversion here.
//            model.addAttribute("error", "Invalid Base64 format. Please provide a valid Base64 encoded payload via the form or use the 'Deserialize from File' option for raw binary testing.");
//            model.addAttribute("serializedDataInput", serializedData);
//            return "deserialize-demo";
//        }
//
//        try {
//            ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes);
//            ObjectInputStream ois = new ObjectInputStream(bis);
//            Object obj = ois.readObject();
//
//            ois.close();
//            bis.close();
//
//            resultMessage = "Deserialization successful. Object type: " + obj.getClass().getName();
//            resultMessage += ", Value (toString) : " + obj.toString();
//
//            model.addAttribute("result", resultMessage);
//
//        } catch (ClassNotFoundException e) {
//            model.addAttribute("error", "Deserialization failed: Class not found. " + e.getMessage());
//            e.printStackTrace();
//        } catch (IOException e) {
//            model.addAttribute("error", "Deserialization failed (IO Error): " + e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e) {
//            model.addAttribute("error", "Deserialization failed: An unexpected error occurred. " + e.getMessage());
//            e.printStackTrace();
//        }
//        model.addAttribute("serializedDataInput", serializedData);
//        return "deserialize-demo";
//    }
//
//    // NEW ENDPOINT TO TRIGGER DESERIALIZATION FROM FILE
//    @PostMapping("/deserialize-from-file")
//    public String doDeserializationFromFile(HttpSession session, Model model) {
//        User currentUser = (User) session.getAttribute("user");
//        if (currentUser == null) return "redirect:/login";
//
//        String resultMessage = "";
//        byte[] payloadBytes;
//
//        try {
//            // Load the resource from the classpath
//            Resource resource = new ClassPathResource(PAYLOAD_FILE_PATH);
//            if (!resource.exists()) {
//                throw new IOException("Payload file not found on classpath: " + PAYLOAD_FILE_PATH);
//            }
//
//            try (InputStream is = resource.getInputStream()) {
//                payloadBytes = is.readAllBytes(); // Read all bytes from the file
//            }
//
//            System.out.println("Payload loaded from classpath file: " + PAYLOAD_FILE_PATH);
//
//            ByteArrayInputStream bis = new ByteArrayInputStream(payloadBytes);
//            ObjectInputStream ois = new ObjectInputStream(bis);
//            Object obj = ois.readObject();
//
//            ois.close();
//            bis.close();
//
//            resultMessage = "Deserialization from file successful. Object type: " + obj.getClass().getName();
//            resultMessage += ", Value (toString) : " + obj.toString();
//
//            model.addAttribute("result", resultMessage);
//
//        } catch (ClassNotFoundException e) {
//            model.addAttribute("error", "Deserialization from file failed: Class not found. " + e.getMessage());
//            e.printStackTrace();
//        } catch (IOException e) {
//            model.addAttribute("error", "Deserialization from file failed (IO Error): " + e.getMessage());
//            e.printStackTrace();
//        } catch (Exception e) {
//            model.addAttribute("error", "Deserialization from file failed: An unexpected error occurred. " + e.getMessage());
//            e.printStackTrace();
//        }
//        model.addAttribute("serializedDataInput", "Payload loaded from " + PAYLOAD_FILE_PATH); // Indicate source
//        return "deserialize-demo";
//    }
//}


import com.example.demo.models.UserSession; // Import your UserSession class
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
/*
@Controller
public class DeserializationController {

    // Handles displaying the deserialization form and current user status
    @GetMapping("/deserialize")
    public String showDeserializationForm(HttpSession session, Model model) {
        // --- TEST SETUP: Ensure a UserSession is in session for demo purposes ---
        // If your application requires a login, ensure a user logs in first.
        // For quick testing, you can temporarily set a dummy UserSession if none exists.
        UserSession currentUserSession = (UserSession) session.getAttribute("user");
        if (currentUserSession == null) {
            // For demo purposes, create a default non-admin session if not logged in
            currentUserSession = new UserSession("demoUser", false);
            session.setAttribute("user", currentUserSession); // Set it in session
            System.out.println("No UserSession found, set a default non-admin demoUser.");
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
        // Ensure a UserSession exists in session for consistency (created in GET mapping for demo)
        UserSession currentUserSession = (UserSession) session.getAttribute("user");
        if (currentUserSession == null) {
            // This should ideally not happen if GET mapping pre-sets it or login is enforced
            return "redirect:/login"; // Or handle as an error
        }

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
                session.setAttribute("user", deserializedUserSession); // Overwriting session 'user' with untrusted data
                resultMessage = "Deserialization successful. UserSession object modified in session. New isAdmin status: " + deserializedUserSession.isAdmin();
            } else {
                // Handle cases where a different type of object was deserialized (for broader testing)
                resultMessage = "Deserialization successful. Object type: " + deserializedObject.getClass().getName();
                resultMessage += ", Value (toString) : " + deserializedObject.toString();
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
        UserSession updatedUserSession = (UserSession) session.getAttribute("user");
        if (updatedUserSession != null) {
            model.addAttribute("currentAdminStatus", updatedUserSession.isAdmin() ? "You are currently an ADMIN." : "You are currently a REGULAR user.");
        }
        return "deserialize-demo"; // Return to the same view
    }
} */
//package com.example.demo.controllers;

import com.example.demo.models.UserSession; // Import your UserSession class
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

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