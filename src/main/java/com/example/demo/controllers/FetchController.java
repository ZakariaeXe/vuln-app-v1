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
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        return "fetch";
    }


    @GetMapping("/fetch-action")
    public String fetchUrlContent(
            @RequestParam String url,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        StringBuilder buf = new StringBuilder();
        try {

            URL u = new URL(url);
            URLConnection cn = u.openConnection();
            cn.setConnectTimeout(5000);
            cn.setReadTimeout(5000);


            System.out.println("SSRF DEBUG: Récupération d'URL (texte) : " + url);

            try (BufferedReader r = new BufferedReader(new InputStreamReader(cn.getInputStream()))) {
                buf.append(r.lines().collect(Collectors.joining("\n")));
            }

            model.addAttribute("result", buf.toString().replace("<","&lt;").replace(">","&gt;"));
        } catch (Exception e) {
            model.addAttribute("error", "Erreur de récupération : " + e.getMessage());
            System.err.println("SSRF DEBUG: Erreur de récupération texte pour " + url + " : " + e.getMessage());
        }
        model.addAttribute("url", url);
        return "fetch";
    }


    @GetMapping("/fetch-image")
    @ResponseBody
    public ResponseEntity<byte[]> fetchImage(
            @RequestParam("imageUrl") String imageUrl,
            HttpSession session
    ) {
        if (session.getAttribute("user") == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            System.out.println("SSRF DEBUG: Récupération de l'URL d'image : " + imageUrl);

            try (InputStream is = conn.getInputStream()) {
                byte[] imageBytes = is.readAllBytes();

                String contentType = conn.getContentType();
                if (contentType == null || contentType.isEmpty() || !contentType.startsWith("image/")) {
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
                System.err.println("SSRF DEBUG: Erreur récupération image - Fichier non trouvé/URL mal formée : " + imageUrl + ", Erreur : " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } catch (IOException e) {
                System.err.println("SSRF DEBUG: Erreur E/S récupération image pour " + imageUrl + ", Erreur : " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            System.err.println("SSRF DEBUG: Erreur générale récupération image pour " + imageUrl + ", Erreur : " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}