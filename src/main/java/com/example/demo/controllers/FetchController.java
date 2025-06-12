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
import java.util.stream.Collectors;

@Controller
public class FetchController {
    @GetMapping("/fetch")
    public String showForm(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        return "fetch"; // Cette vue affichera les deux formulaires
    }

    // Votre méthode existante pour récupérer du contenu TEXTUEL (reste utile pour file:/// etc.)
    @GetMapping("/fetch-action")
    public String fetchUrlContent(
            @RequestParam String url,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        StringBuilder buf = new StringBuilder();
        try {
            // SSRF: aucune validation d'URL
            URL u = new URL(url);
            URLConnection cn = u.openConnection();
            cn.setConnectTimeout(5000); // Temps max pour la connexion
            cn.setReadTimeout(5000);    // Temps max pour la lecture des données

            // Pour le débogage/analyse serveur
            System.out.println("SSRF DEBUG: Récupération d'URL (texte) : " + url);

            try (BufferedReader r = new BufferedReader(new InputStreamReader(cn.getInputStream()))) {
                buf.append(r.lines().collect(Collectors.joining("\n")));
            }
            // Nous encodons toujours le HTML pour éviter la XSS dans le contenu récupéré
            model.addAttribute("result", buf.toString().replace("<","&lt;").replace(">","&gt;"));
        } catch (Exception e) {
            model.addAttribute("error", "Erreur de récupération : " + e.getMessage());
            System.err.println("SSRF DEBUG: Erreur de récupération texte pour " + url + " : " + e.getMessage());
        }
        model.addAttribute("url", url); // Conserver l'URL dans le formulaire
        return "fetch"; // Rend la vue fetch.html
    }

    // NOUVEAU : Méthode pour le proxy d'image (renvoie des données binaires)
    @GetMapping("/fetch-image")
    @ResponseBody // TRÈS IMPORTANT : Indique à Spring de renvoyer le byte[] directement comme corps de réponse HTTP
    public ResponseEntity<byte[]> fetchImage(
            @RequestParam("imageUrl") String imageUrl,
            HttpSession session
    ) {
        // Vérification d'authentification (important pour le contexte de votre application)
        if (session.getAttribute("user") == null) {
            // Ne peut pas rediriger depuis @ResponseBody, renvoie une erreur 401
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            // SSRF : Aucune validation ou assainissement de l'URL d'image
            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // Pour le débogage/analyse serveur
            System.out.println("SSRF DEBUG: Récupération de l'URL d'image : " + imageUrl);

            // Lire les octets de l'image
            try (InputStream is = conn.getInputStream()) {
                byte[] imageBytes = is.readAllBytes();

                // Tenter de déterminer le Content-Type (méthode simple)
                String contentType = conn.getContentType();
                if (contentType == null || contentType.isEmpty() || !contentType.startsWith("image/")) {
                    // Fallback si le type n'est pas donné ou n'est pas une image
                    if (imageUrl.toLowerCase().endsWith(".png")) contentType = "image/png";
                    else if (imageUrl.toLowerCase().endsWith(".jpg") || imageUrl.toLowerCase().endsWith(".jpeg")) contentType = "image/jpeg";
                    else if (imageUrl.toLowerCase().endsWith(".gif")) contentType = "image/gif";
                    else contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // Type binaire générique
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.setContentLength(imageBytes.length);

                return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

            } catch (FileNotFoundException | MalformedURLException e) {
                // Erreur spécifique pour les fichiers non trouvés ou URL mal formées
                System.err.println("SSRF DEBUG: Erreur récupération image - Fichier non trouvé/URL mal formée : " + imageUrl + ", Erreur : " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
            } catch (IOException e) {
                // Erreur générale d'E/S (timeout, problème réseau, etc.)
                System.err.println("SSRF DEBUG: Erreur E/S récupération image pour " + imageUrl + ", Erreur : " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            }

        } catch (Exception e) {
            // Pour les erreurs de création d'URL, de sécurité génériques
            System.err.println("SSRF DEBUG: Erreur générale récupération image pour " + imageUrl + ", Erreur : " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 400 Bad Request
        }
    }
}