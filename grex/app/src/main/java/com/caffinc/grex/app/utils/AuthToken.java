package com.caffinc.grex.app.utils;

import java.util.UUID;

/**
 * Singleton to hold or generate a new random Auth Token
 *
 * @author Sriram
 */
public class AuthToken {
    private static final AuthToken INSTANCE = new AuthToken();
    private String authToken = UUID.randomUUID().toString();

    private AuthToken() {
    }

    public static AuthToken getInstance() {
        return INSTANCE;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }
}
