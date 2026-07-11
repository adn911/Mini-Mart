package com.minimart.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private String adminUsername;
    private String adminPassword;

    public AdminAuthService(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadCredentials() {
        try (InputStream is = getClass().getResourceAsStream("/admin-credentials.json")) {
            Map<String, String> creds = objectMapper.readValue(
                is, new TypeReference<Map<String, String>>() {});
            this.adminUsername = creds.get("username");
            this.adminPassword = creds.get("password");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load admin credentials", e);
        }
    }

    public String login(String username, String password) {
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            return jwtUtil.generateToken(username);
        }
        return null;
    }
}
