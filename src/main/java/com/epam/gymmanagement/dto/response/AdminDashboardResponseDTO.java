package com.epam.gymmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponseDTO {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long admins;
    private long trainees;
    private long trainers;
    private long trainings;
    private List<AdminUserSummaryResponseDTO> users;
}
