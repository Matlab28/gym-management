package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationResponseDTO {
    private String username;
    private String password;

    public RegistrationResponseDTO() {
    }

    public RegistrationResponseDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
