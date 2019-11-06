package com.neodynamica.userinterface.cli;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Cross-platform prompt for nda commands without any complications from the system shell, can be run directly from the IDE
 */
public class CLIPrompt {

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile(" ");
        String[] words;

        Scanner in = new Scanner(System.in);
        String line = "";

        while (!"exit".equalsIgnoreCase(line)) {
            System.out.print("$ ");
            line = in.nextLine();
            words = pattern.split(line);

            if ("nda".equals(words[0])) {
                words = Arrays.copyOfRange(words, 1, words.length);
                CLI.main(words);
            } else {
                System.out.println(
                        "This prompt is only to run 'nda' commands and cannot run '" + words[0]
                                + "'.");
            }
            System.out.println("");
        }
    }
}
