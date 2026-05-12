package com.epam.gymmanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponseDTO {
    private String message;

    public MessageResponseDTO() {
    }

    public MessageResponseDTO(String message) {
        this.message = message;
    }
}
