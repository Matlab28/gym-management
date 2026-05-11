package com.epam.gymmanagement.util;

import com.epam.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsernameGenerator {
    private final UserRepository userRepository;

    public String generate(String firstName, String lastName) {
        String baseUsername = (firstName + "." + lastName)
                .toLowerCase()
                .replaceAll("\\s+", "");

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}