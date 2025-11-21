package com.rudraksha.authservice.dto;

import com.rudraksha.authservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Builder
public class RegisterResponseDTO {
    private UUID id;
    private String email;
    private Role role;
    private String message;
    private String accessToken;
    private String refreshToken;
}
