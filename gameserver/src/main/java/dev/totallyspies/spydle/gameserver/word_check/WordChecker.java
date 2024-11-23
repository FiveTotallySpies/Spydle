package dev.totallyspies.spydle.gameserver.word_check;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class WordChecker {
    public static boolean checkWord(String wordToCheck, String substringToCheck) {
        // File containing the list of words (CSV format)
        String fileName = "words.csv";

        // Load words from the CSV file into a Set for fast lookup
        Set<String> words = loadWordsFromCSV(fileName);

        // Check if the word exists in the list and contains the given substring
        if (words.contains(wordToCheck) && wordToCheck.contains(substringToCheck)) {
            System.out.println("The word '" + wordToCheck + "' exists and contains the substring '" + substringToCheck + "'.");
            return true;
        } else if (words.contains(wordToCheck)) {
            System.out.println("The word '" + wordToCheck + "' exists but does NOT contain the substring '" + substringToCheck + "'.");
        } else {
            System.out.println("The word '" + wordToCheck + "' does not exist in the list.");
        }
        return false;
    }

    private static Set<String> loadWordsFromCSV(String fileName) {
        Set<String> words = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split by commas and trim whitespace
                String[] lineWords = line.split(",");
                words.addAll(Arrays.stream(lineWords).map(String::trim).collect(Collectors.toSet()));
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
        return words;
    }
}

