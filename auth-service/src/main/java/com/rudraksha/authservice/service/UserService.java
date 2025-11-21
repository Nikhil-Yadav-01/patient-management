package com.rudraksha.authservice.service;

import com.rudraksha.authservice.dto.RegisterRequestDTO;
import com.rudraksha.authservice.dto.RegisterResponseDTO;
import com.rudraksha.authservice.model.User;
import com.rudraksha.authservice.repository.UserRepository;
import com.rudraksha.authservice.util.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ==========================================
    // FIND USER BY EMAIL
    // ==========================================
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ==========================================
    // REGISTER USER
    // ==========================================
    public RegisterResponseDTO registerUser(RegisterRequestDTO dto) {

        // Check if user exists
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(dto.getEmail());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setRole(dto.getRole());

        User saved = userRepository.save(newUser);

        // Generate access + refresh tokens
        String accessToken = jwtUtil.generateAccessToken(saved.getEmail(), saved.getRole().toString());
        String refreshToken = jwtUtil.generateRefreshToken(saved.getEmail());

        // Build and return response DTO
        return RegisterResponseDTO.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("User registered successfully")
                .build();
    }
}
