package com.example.demo.models;

import java.io.Serializable;

public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private boolean isAdmin;  // Propriété qu'un attaquant pourrait manipuler

    // Constructeur
    public UserSession(String username, boolean isAdmin) {
        this.username = username;
        this.isAdmin = isAdmin;
    }

    // Getters et Setters
    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "username='" + username + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}



