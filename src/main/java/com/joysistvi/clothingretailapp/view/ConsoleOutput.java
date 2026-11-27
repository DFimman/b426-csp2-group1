package com.joysistvi.clothingretailapp.view;

public final class ConsoleOutput {

    private ConsoleOutput() {}

    // Clears the console in terminals with ANSI support
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Keep operation results visible before the next screen is cleared
    public static boolean pause(java.util.Scanner scanner) {
        System.out.print("\nPress Enter to continue...");
        try {
            scanner.nextLine();
            return true;
        } catch (java.util.NoSuchElementException | IllegalStateException e) {
            return false;
        }
    }

    // Shorten long strings only for display
    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        if (maxLength <= 3) return text.substring(0, maxLength);
        return text.substring(0, maxLength - 3) + "...";
    }
}
