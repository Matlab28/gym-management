package com.epam.gymmanagement.dto2.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UserResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String username;
    private boolean isActive;
}
