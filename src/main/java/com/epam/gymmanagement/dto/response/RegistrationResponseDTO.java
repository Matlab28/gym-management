package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RegistrationResponseDTO {
    private UUID registrationId;
    private String username;
    private String password;

    public RegistrationResponseDTO() {
    }

    public RegistrationResponseDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
