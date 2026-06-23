package com.epam.gymmanagement.service;

import com.epam.gymmanagement.dto.request.ProfileRequestDTO;
import com.epam.gymmanagement.dto.response.UserProfileResponseDTO;
import com.epam.gymmanagement.dto.response.UserSummaryResponseDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Qualifier("userService")
public interface UserService {
    UserSummaryResponseDTO createUser(ProfileRequestDTO dto);

    List<UserSummaryResponseDTO> getAllUsers();

    List<UserProfileResponseDTO> getAllUserProfiles();

    UserProfileResponseDTO getUserProfile(UUID userId);

    UserSummaryResponseDTO getUserById(UUID userId);

    UserProfileResponseDTO updateUserProfile(UUID userId, UserProfileResponseDTO dto);

    String deleteUser(UUID userId);
}
