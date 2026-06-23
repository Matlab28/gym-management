package com.epam.gymmanagement.security;

import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {
    @Mock
    private UserRepository userRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUsernameResolvesEmailPrincipalToStoredUsername() {
        SecurityService securityService = new SecurityService(userRepository);
        UserEntity user = user("email.owner", "owner@gmail.com");

        authenticate("owner@gmail.com", UserRole.TRAINEE);
        when(userRepository.findByEmailIgnoreCase("owner@gmail.com")).thenReturn(Optional.of(user));

        assertEquals("email.owner", securityService.currentUsername());
        assertDoesNotThrow(securityService::requireAuthenticated);
    }

    @Test
    void requireSameUserOrAdminAllowsSelfAndRejectsOtherUsers() {
        SecurityService securityService = new SecurityService(userRepository);
        authenticate("self.user", UserRole.TRAINEE);

        assertDoesNotThrow(() -> securityService.requireSameUserOrAdmin("self.user"));
        assertThrows(
                AccessDeniedException.class,
                () -> securityService.requireSameUserOrAdmin("other.user")
        );
    }

    private void authenticate(String principal, UserRole role) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private UserEntity user(String username, String email) {
        return UserEntity.builder()
                .username(username)
                .email(email)
                .firstName("First")
                .lastName("Last")
                .password("encoded-password")
                .isActive(true)
                .profileStatus(ProfileStatus.ACTIVE)
                .role(UserRole.TRAINEE)
                .build();
    }
}
