package com.epam.gymmanagement.security;

import com.epam.gymmanagement.constant.UserRole;
import com.epam.gymmanagement.constant.ProfileStatus;
import com.epam.gymmanagement.entity.UserEntity;
import com.epam.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GymUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserRoleResolver userRoleResolver;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        UserEntity user = findByEmailOrUsername(usernameOrEmail);

        if (user.getProfileStatus() == ProfileStatus.PENDING) {
            throw new DisabledException("User profile is pending confirmation");
        } else if (user.getProfileStatus() == ProfileStatus.INACTIVE) {
            throw new DisabledException("User profile is inactive");
        } else if (user.getProfileStatus() == ProfileStatus.SUSPENDED) {
            throw new DisabledException("User profile is suspended");
        } else if (user.getProfileStatus() == ProfileStatus.DELETED) {
            throw new DisabledException("User profile is deleted");
        }

        UserRole role = userRoleResolver.resolve(user);
        String principalName = principalName(usernameOrEmail, user);

        return new User(
                principalName,
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }

    private UserEntity findByEmailOrUsername(String usernameOrEmail) {
        if (usernameOrEmail != null && usernameOrEmail.contains("@")) {
            return userRepository.findByEmailIgnoreCase(usernameOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
        }

        return userRepository.findByUsernameIgnoreCaseContains(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
    }

    private String principalName(String usernameOrEmail, UserEntity user) {
        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(usernameOrEmail)) {
            return user.getEmail();
        }

        return user.getUsername();
    }
}
