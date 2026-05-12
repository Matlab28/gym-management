package com.epam.gymmanagement.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String CHARACTERS = UPPERCASE + LOWERCASE + DIGITS;
    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        char[] password = new char[PASSWORD_LENGTH];
        password[0] = randomCharacter(UPPERCASE);
        password[1] = randomCharacter(LOWERCASE);
        password[2] = randomCharacter(DIGITS);

        for (int i = 3; i < PASSWORD_LENGTH; i++) {
            password[i] = randomCharacter(CHARACTERS);
        }

        shuffle(password);
        return new String(password);
    }

    private char randomCharacter(String source) {
        return source.charAt(secureRandom.nextInt(source.length()));
    }

    private void shuffle(char[] password) {
        for (int i = password.length - 1; i > 0; i--) {
            int index = secureRandom.nextInt(i + 1);
            char current = password[i];
            password[i] = password[index];
            password[index] = current;
        }
    }
}
