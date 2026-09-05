package com.mentortrack.service;

import com.mentortrack.domain.AdminUser;
import com.mentortrack.domain.Role;
import com.mentortrack.domain.Student;
import com.mentortrack.dto.auth.LoginResponse;
import com.mentortrack.exception.UnauthorizedException;
import com.mentortrack.repository.AdminUserRepository;
import com.mentortrack.repository.StudentRepository;
import com.mentortrack.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(StudentRepository studentRepository,
                        AdminUserRepository adminUserRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.studentRepository = studentRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Tries to authenticate as an admin first (admin usernames are distinct from reg numbers
     * in the seed data), then falls back to student reg-no/password.
     */
    public LoginResponse login(String username, String rawPassword) {
        var adminOpt = adminUserRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            AdminUser admin = adminOpt.get();
            if (!passwordEncoder.matches(rawPassword, admin.getPasswordHash())) {
                throw new UnauthorizedException("Invalid credentials");
            }
            String token = jwtService.generateToken(admin.getUsername(), Role.ADMIN.name());
            return new LoginResponse(token, Role.ADMIN.name(), false);
        }

        Student student = studentRepository.findByRegNo(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, student.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.generateToken(student.getRegNo(), Role.STUDENT.name());
        return new LoginResponse(token, Role.STUDENT.name(), student.isForcePasswordChange());
    }

    @Transactional
    public void changeStudentPassword(String regNo, String currentPassword, String newPassword) {
        Student student = studentRepository.findByRegNo(regNo)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(currentPassword, student.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }
        student.setPasswordHash(passwordEncoder.encode(newPassword));
        student.setForcePasswordChange(false);
        studentRepository.save(student);
    }
}
