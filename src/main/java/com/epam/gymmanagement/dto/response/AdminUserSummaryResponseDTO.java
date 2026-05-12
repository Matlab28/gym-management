package com.epam.gymmanagement.dto.response;

import com.epam.gymmanagement.constant.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserSummaryResponseDTO {
    private String username;
    private String firstName;
    private String lastName;
    private Boolean active;
    private UserRole role;
}
