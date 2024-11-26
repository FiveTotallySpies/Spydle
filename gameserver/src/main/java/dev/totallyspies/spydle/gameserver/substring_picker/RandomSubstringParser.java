package dev.totallyspies.spydle.gameserver.substring_picker;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class RandomSubstringParser {
    public static String pickRandomSubstring() {
        // File containing substrings (CSV format)
        String fileName = "substrings.csv";

        // Load substrings from the file into a list
        List<String> substrings = loadSubstringsFromCSV(fileName);

        // Pick a random substring if the list is not empty
        if (!substrings.isEmpty()) {
            Random random = new Random();
            String randomSubstring = substrings.get(random.nextInt(substrings.size()));
            System.out.println("Randomly picked substring: " + randomSubstring);
            return randomSubstring;
        } else {
            System.out.println("No substrings found in the file " + fileName);
            return "Not Substrings Found in the file" + fileName;
        }
    }

    private static List<String> loadSubstringsFromCSV(String fileName) {
        List<String> substrings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split by commas and trim whitespace
                String[] lineSubstrings = line.split(",");
                substrings.addAll(Arrays.stream(lineSubstrings).map(String::trim).toList());
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return substrings;
    }
}
