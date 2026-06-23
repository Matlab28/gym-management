package com.epam.gymmanagement.security;

import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final UserRepository userRepository;

    public void requireAuthenticated() {
        currentUsername();
    }

    public String currentUsername() {
        String principalName = currentAuthentication().getName();

        if (principalName.contains("@")) {
            return userRepository.findByEmailIgnoreCase(principalName)
                    .map(UserEntity::getUsername)
                    .orElse(principalName);
        }

        return principalName;
    }

    public boolean hasRole(UserRole role) {
        Authentication authentication = currentAuthentication();
        String authority = toAuthority(role);

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    public void requireRole(UserRole... roles) {
        Authentication authentication = currentAuthentication();
        Set<String> requiredAuthorities = Arrays.stream(roles)
                .map(this::toAuthority)
                .collect(Collectors.toSet());

        boolean allowed = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredAuthorities::contains);

        if (!allowed) {
            throw new AccessDeniedException("Required role: " + requiredAuthorities);
        }
    }

    public void requireSelfOrAdmin(String username, UserRole selfRole) {
        String currentUsername = currentUsername();

        if (hasRole(UserRole.ADMIN)) {
            return;
        }

        if (currentUsername.equals(username) && hasRole(selfRole)) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    public void requireSameUserOrAdmin(String username) {
        String currentUsername = currentUsername();

        if (hasRole(UserRole.ADMIN) || currentUsername.equals(username)) {
            return;
        }

        throw new AccessDeniedException("Access denied");
    }

    private Authentication currentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authentication is required");
        }

        return authentication;
    }

    private String toAuthority(UserRole role) {
        return "ROLE_" + role.name();
    }
}
