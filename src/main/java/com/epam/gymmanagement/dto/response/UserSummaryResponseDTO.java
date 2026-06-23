package com.epam.gymmanagement.dto.response;

import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummaryResponseDTO {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private String specialization;
    private Boolean active;
    private ProfileStatus profileStatus;
    private UserRole role;
}
