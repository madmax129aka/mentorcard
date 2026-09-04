package com.mentortrack.dto.auth;

public class LoginResponse {

    private String token;
    private String role;
    private boolean forcePasswordChange;

    public LoginResponse(String token, String role, boolean forcePasswordChange) {
        this.token = token;
        this.role = role;
        this.forcePasswordChange = forcePasswordChange;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }
}
