package com.authetication.project.model;

public class AuthResponse {
    private String token;
    private String message;
    private String userName;

    public AuthResponse(String token, String message, String userName) {
        this.token = token;
        this.message = message;
        this.userName = userName;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public String getUserName() {
        return userName;
    }
}
