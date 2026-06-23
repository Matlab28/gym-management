package com.epam.gymmanagement.dto.response;

import com.epam.gymmanagement.constant.ProfileStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponseDTO {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private String specialization;
    private ProfileStatus profileStatus;
    private List<UserSummaryResponseDTO> trainers;
    private List<UserSummaryResponseDTO> trainees;
}
