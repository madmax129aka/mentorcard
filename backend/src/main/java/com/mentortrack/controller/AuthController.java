package com.mentortrack.controller;

import com.mentortrack.dto.auth.ChangePasswordRequest;
import com.mentortrack.dto.auth.LoginRequest;
import com.mentortrack.dto.auth.LoginResponse;
import com.mentortrack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/change-password")
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        String regNo = authentication.getName();
        authService.changeStudentPassword(regNo, request.getCurrentPassword(), request.getNewPassword());
    }
}
