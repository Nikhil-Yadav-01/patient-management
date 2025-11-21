package com.rudraksha.authservice.dto;

import com.rudraksha.authservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class RegisterRequestDTO {
    private String email;
    private String password;
    private Role role; // PATIENT, ADMIN, DOCTOR
}
