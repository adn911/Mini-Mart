package com.minimart.security;

import com.minimart.repository.AdminUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final JwtUtil jwtUtil;
    private final AdminUserRepository adminUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminAuthService(JwtUtil jwtUtil, AdminUserRepository adminUserRepository) {
        this.jwtUtil = jwtUtil;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String login(String username, String password) {
        return adminUserRepository.findByUsername(username)
            .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
            .map(user -> jwtUtil.generateToken(user.getUsername()))
            .orElse(null);
    }
}
