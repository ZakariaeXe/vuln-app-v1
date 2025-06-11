package com.example.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource; // Import for InputSource
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

// Necessary Imports for XML parsing
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;

import com.example.demo.models.User; // Assuming User model is needed for session check

@Controller
public class XmlProcessorController {

    @GetMapping("/xml-process")
    public String showXmlProcessorForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login"; // Must be logged in to access
        }
        model.addAttribute("result", "Submit your XML below.");
        return "xml-processor";
    }

    @PostMapping(value = "/xml-process", consumes = "application/xml", produces = "text/html")
    public String processXml(@RequestBody String xmlData, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login"; // Must be logged in to access
        }

        StringBuilder parsedContent = new StringBuilder();
        try {
            // XXE Vulnerability: Default DocumentBuilderFactory is used without
            // explicit disabling of DTDs or external entity resolution.
            // This setup is vulnerable to XXE attacks.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // To fix XXE, you would typically add:
            // dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            // dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);


            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xmlData)));

            doc.getDocumentElement().normalize();

            // Example: Extracting data from elements
            NodeList titleNodes = doc.getElementsByTagName("title");
            for (int i = 0; i < titleNodes.getLength(); i++) {
                Node node = titleNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    parsedContent.append("Title: ").append(node.getTextContent()).append("<br>");
                }
            }

            NodeList contentNodes = doc.getElementsByTagName("content");
            for (int i = 0; i < contentNodes.getLength(); i++) {
                Node node = contentNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    parsedContent.append("Content: ").append(node.getTextContent()).append("<br>");
                }
            }

            if (parsedContent.length() == 0) {
                parsedContent.append("XML parsed. No 'title' or 'content' elements found, or injected content processed.");
                parsedContent.append("<br>Raw XML Processed (Potential XXE data): ").append(xmlData.replace("<", "&lt;").replace(">", "&gt;"));
            }

            model.addAttribute("result", parsedContent.toString());

        } catch (ParserConfigurationException | SAXException | IOException e) {
            model.addAttribute("error", "Error processing XML: " + e.getMessage());
            e.printStackTrace();
        }
        return "xml-processor";
    }
}