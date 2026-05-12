package com.epam.gymmanagement.security;

import com.epam.gymmanagement.constant.UserRole;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("User profile is inactive");
        }

        UserRole role = userRoleResolver.resolve(user);

        return new User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }
}
