package com.rudraksha.authservice.service;

import com.rudraksha.authservice.dto.LoginRequestDTO;
import com.rudraksha.authservice.dto.LoginResponseDTO;
import com.rudraksha.authservice.dto.RefreshTokenResponseDTO;
import com.rudraksha.authservice.model.Role;
import com.rudraksha.authservice.model.User;
import com.rudraksha.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // LOGIN → Generate Access + Refresh token
    public Optional<LoginResponseDTO> authenticate(LoginRequestDTO dto) {

        return userService.findByEmail(dto.getEmail())
                .filter(u -> passwordEncoder.matches(dto.getPassword(), u.getPassword()))
                .map(u -> new LoginResponseDTO(
                        jwtUtil.generateAccessToken(u.getEmail(), u.getRole().toString()),
                        jwtUtil.generateRefreshToken(u.getEmail()),
                        u.getRole()
                ));
    }

    // VALIDATE ACCESS TOKEN ONLY
    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return "ACCESS".equals(jwtUtil.extractType(token));
        } catch (JwtException e) {
            return false;
        }
    }

    // REFRESH TOKEN → generate new access + refresh
    public Optional<RefreshTokenResponseDTO> refreshToken(String refreshToken) {
        try {
            jwtUtil.validateToken(refreshToken);

            if (!"REFRESH".equals(jwtUtil.extractType(refreshToken))) {
                return Optional.empty();
            }

            String email = jwtUtil.extractUsername(refreshToken);
            User user = userService.findByEmail(email).orElseThrow();

            // Issue new tokens
            return Optional.of(new RefreshTokenResponseDTO(
                    jwtUtil.generateAccessToken(email, user.getRole().toString()),
                    jwtUtil.generateRefreshToken(email)
            ));

        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    // -------------------------
    // Generate short-lived access token (15 minutes)
    // -------------------------
    public String generateAccessToken(String email, Role role) {
        return jwtUtil.generateAccessToken(email, role.name());
    }

    // -------------------------
    // Generate long-lived refresh token (30 days)
    // -------------------------
    public String generateRefreshToken(String email) {
        return jwtUtil.generateRefreshToken(email);
    }
}
