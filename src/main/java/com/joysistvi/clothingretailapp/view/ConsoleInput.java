package com.joysistvi.clothingretailapp.view;
import java.math.BigDecimal;
import java.util.Scanner;

// Read and validate basic console input.
public class ConsoleInput {
    private final Scanner scanner;
    public ConsoleInput(Scanner scanner) { this.scanner = scanner; }
    public String text(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    public int integer(String prompt) { return Integer.parseInt(text(prompt)); }
    public int positive(String prompt) {
        int number = integer(prompt);
        if (number <= 0) throw new IllegalArgumentException("Enter a positive whole number");
        return number;
    }
    public BigDecimal money(String prompt) { return new BigDecimal(text(prompt)); }
}
