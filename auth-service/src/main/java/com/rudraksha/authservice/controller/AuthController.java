package com.rudraksha.authservice.controller;

import com.rudraksha.authservice.dto.*;
import com.rudraksha.authservice.model.Role;
import com.rudraksha.authservice.service.AuthService;
import com.rudraksha.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Auth Service Running");
    }

    // ============================
    // LOGIN
    // ============================
    @PostMapping("/login")
    @Operation(summary = "User login and generate access + refresh tokens")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {

        Optional<LoginResponseDTO> response = authService.authenticate(request);

        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // ============================
    // REGISTER USER
    // ============================
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<RegisterResponseDTO> register(
            @RequestBody RegisterRequestDTO requestDTO) {

        // Only allow PATIENT role for self-registration
        if (requestDTO.getRole() != Role.PATIENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body( RegisterResponseDTO.builder()
                            .message("You cannot register with role " + requestDTO.getRole())
                            .build()
                    );
        }

        RegisterResponseDTO newUser = userService.registerUser(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/create")
    @Operation(summary = "Admin creates staff accounts (DOCTOR, NURSE, ADMIN)")
    public ResponseEntity<RegisterResponseDTO> createStaff(@RequestBody RegisterRequestDTO requestDTO) {

        // Only allow privileged roles for admin-created accounts
        if (requestDTO.getRole() == Role.PATIENT) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null); // Patients should self-register only
        }

        // Optionally, validate role is one of the allowed roles
        if (!(requestDTO.getRole() == Role.DOCTOR ||
                requestDTO.getRole() == Role.NURSE ||
                requestDTO.getRole() == Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        // Create the user
        RegisterResponseDTO newStaff = userService.registerUser(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(newStaff);
    }

    // ============================
    // REFRESH TOKEN
    // ============================
    @PostMapping("/refresh")
    @Operation(summary = "Generate new access + refresh token using old refresh token")
    public ResponseEntity<RefreshTokenResponseDTO> refresh(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String refreshToken = authHeader.substring(7);

        Optional<RefreshTokenResponseDTO> newTokens = authService.refreshToken(refreshToken);

        return newTokens.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // ============================
    // VALIDATE ACCESS TOKEN
    // ============================
    @GetMapping("/validate")
    @Operation(summary = "Validate access token")
    public ResponseEntity<Void> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isValid = authService.validateToken(authHeader.substring(7));

        return isValid ? ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}