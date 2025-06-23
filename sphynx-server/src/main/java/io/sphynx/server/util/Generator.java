package io.sphynx.server.util;

import java.security.SecureRandom;

public class Generator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+[]{};:,.?";
    private static final SecureRandom random = new SecureRandom();

    public static String GenerateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }

        return sb.toString();
    }
}
