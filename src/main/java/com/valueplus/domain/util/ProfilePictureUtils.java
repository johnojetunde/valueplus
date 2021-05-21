package com.valueplus.domain.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class ProfilePictureUtils {

    private static final String DEFAULT_PIC_PATH = "/default-pic.txt";

    private ProfilePictureUtils() {
    }

    public static String defaultImageBase64() throws IOException {
        String line;
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(ProfilePictureUtils.class.getResourceAsStream(DEFAULT_PIC_PATH)))) {
            line = br.readLine();
        }

        return line;
    }
}
