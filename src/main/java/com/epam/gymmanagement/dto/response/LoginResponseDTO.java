package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LoginResponseDTO {
    private UUID loginId;
    private String message;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String message) {
        this.message = message;
    }
}
