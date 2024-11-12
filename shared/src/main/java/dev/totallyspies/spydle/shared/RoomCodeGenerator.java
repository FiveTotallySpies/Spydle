package dev.totallyspies.spydle.shared;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RoomCodeGenerator {

    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generateRandomCode() {
        int length = 5;
        StringBuilder result = new StringBuilder(length);
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }

}
