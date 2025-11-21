package com.rudraksha.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RefreshTokenResponseDTO {

    private String accessToken;
    private String refreshToken;
}
