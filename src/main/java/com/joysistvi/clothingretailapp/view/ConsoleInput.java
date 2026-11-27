package com.joysistvi.clothingretailapp.view;
import java.math.BigDecimal;
import java.util.Scanner;

// Read and validate basic console input.
public class ConsoleInput {
    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) { this.scanner = scanner; }

    // Reads a trimmed line of console input.
    public String text(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Reads a whole number from console input.
    public int integer(String prompt) { return Integer.parseInt(text(prompt)); }

    // Reads a positive whole number from console input.
    public int positive(String prompt) {
        int number = integer(prompt);
        if (number <= 0) throw new IllegalArgumentException("Enter a positive whole number");
        return number;
    }

    // Reads a monetary amount from console input.
    public BigDecimal money(String prompt) { return new BigDecimal(text(prompt)); }
}
