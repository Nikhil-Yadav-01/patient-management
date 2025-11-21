package com.rudraksha.authservice;

import com.rudraksha.authservice.dto.*;
import com.rudraksha.authservice.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    public void testRegisterLoginRefreshValidateFlow() {
        // 1. Register a new patient
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "patient3@example.com",
                "password123",
                Role.PATIENT
        );

        ResponseEntity<RegisterResponseDTO> registerResponse = restTemplate.postForEntity(
                baseUrl() + "/register",
                registerRequest,
                RegisterResponseDTO.class
        );

        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        RegisterResponseDTO registeredUser = registerResponse.getBody();
        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getEmail()).isEqualTo("patient3@example.com");
        assertThat(registeredUser.getRole()).isEqualTo(Role.PATIENT);
        assertThat(registeredUser.getAccessToken()).isNotEmpty();
        assertThat(registeredUser.getRefreshToken()).isNotEmpty();

        // 2. Login with same user
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("patient3@example.com");
        loginRequest.setPassword("password123");

        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate.postForEntity(
                baseUrl() + "/login",
                loginRequest,
                LoginResponseDTO.class
        );

        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoginResponseDTO loginBody = loginResponse.getBody();
        assertThat(loginBody).isNotNull();
        assertThat(loginBody.getAccessToken()).isNotEmpty();
        assertThat(loginBody.getRefreshToken()).isNotEmpty();
        assertThat(loginBody.getRole()).isEqualTo(Role.PATIENT);

        // 3. Refresh token
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + loginBody.getRefreshToken());
        HttpEntity<Void> refreshRequest = new HttpEntity<>(headers);

        ResponseEntity<RefreshTokenResponseDTO> refreshResponse = restTemplate.exchange(
                baseUrl() + "/refresh",
                HttpMethod.POST,
                refreshRequest,
                RefreshTokenResponseDTO.class
        );

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        RefreshTokenResponseDTO newTokens = refreshResponse.getBody();
        assertThat(newTokens).isNotNull();
        assertThat(newTokens.getAccessToken()).isNotEmpty();
        assertThat(newTokens.getRefreshToken()).isNotEmpty();

        // 4. Validate access token
        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + newTokens.getAccessToken());
        HttpEntity<Void> validateRequest = new HttpEntity<>(headers);

        ResponseEntity<Void> validateResponse = restTemplate.exchange(
                baseUrl() + "/validate",
                HttpMethod.GET,
                validateRequest,
                Void.class
        );

        assertThat(validateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testRegisterPatientWithAdminRoleForbidden() {
        RegisterRequestDTO adminRequest = new RegisterRequestDTO(
                "admin@example.com",
                "password123",
                Role.ADMIN
        );

        ResponseEntity<RegisterResponseDTO> response = restTemplate.postForEntity(
                baseUrl() + "/register",
                adminRequest,
                RegisterResponseDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("You cannot register with role ADMIN");
    }

    @Test
    public void testValidateInvalidTokenUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalidToken");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/validate",
                HttpMethod.GET,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
