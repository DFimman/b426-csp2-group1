package com.joysistvi.clothingretailapp.View;

import com.joysistvi.clothingretailapp.Controller.Product_Variant;
import com.joysistvi.clothingretailapp.Service.Product_VariantService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import static com.joysistvi.clothingretailapp.View.ConsoleOutput.clearScreen;
import static com.joysistvi.clothingretailapp.View.ConsoleOutput.truncate;

public class Product_VariantView {

    private final Product_VariantService variantService;
    private final Scanner scanner;

    public Product_VariantView(Product_VariantService variantService, Scanner scanner) {
        this.variantService = variantService;
        this.scanner = scanner;
    }

    public void handleMenu() {
        while (true) {
            clearScreen();
            System.out.println("\n=== PRODUCT CATALOG ===");
            System.out.println("1. Browse All Items");
            System.out.println("2. Search by Size or Color");
            System.out.println("0. Back");

            Integer choice = promptForInt("Choose an option: ");
            if (choice == null) {
                return;
            }

            switch (choice) {
                case 1 -> displayCatalog();
                case 2 -> promptSearch();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
            if (!ConsoleOutput.pause(scanner)) return;
        }
    }

    private void displayCatalog() {
        clearScreen();
        List<Product_Variant> variants = variantService.getAllActiveVariants();
        printCatalog(variants, "Available Items");
    }

    private void promptSearch() {
        clearScreen();
        System.out.println("\n----- Search Items -----");
        String keyword = getStringInput("Enter size or color: ");
        if (keyword == null) {
            return;
        }

        String search = keyword.trim().toLowerCase();
        List<Product_Variant> matches = new ArrayList<>();
        for (Product_Variant variant : variantService.getAllActiveVariants()) {
            boolean sizeMatches = variant.getSize() != null && variant.getSize().toLowerCase().contains(search);
            boolean colorMatches = variant.getColor() != null && variant.getColor().toLowerCase().contains(search);
            if (sizeMatches || colorMatches) {
                matches.add(variant);
            }
        }
        printCatalog(matches, "Search Results");
    }

    private void printCatalog(List<Product_Variant> variants, String title) {
        System.out.println("\n----- " + title + " -----");
        System.out.printf("%-8s| %-8s| %-12s| %-10s| %-14s%n", "Product", "Size", "Color", "Price", "Availability");

        if (variants == null || variants.isEmpty()) {
            System.out.println("No items found.");
            return;
        }

        for (Product_Variant variant : variants) {
            System.out.printf("%-8d| %-8s| %-12s| %-10.2f| %-14s%n",
                    variant.getProduct_id(),
                    truncate(variant.getSize(), 8),
                    truncate(variant.getColor(), 12),
                    variant.getUnit_prize(),
                    variant.getStock_quantity() > 0 ? "In stock" : "Out of stock");
        }
    }

    private Integer promptForInt(String prompt) {
        while (true) {
            String input = getStringInput(prompt);
            if (input == null) {
                return null;
            }

            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("\nValidation Error: Please enter a valid whole number.");
            }
        }
    }

    private String getStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                String input = scanner.nextLine();

                if (input.trim().isEmpty()) {
                    System.out.println("\nValidation Error: Input cannot be blank.");
                    continue;
                }

                return input;
            } catch (NoSuchElementException | IllegalStateException e) {
                System.out.println("\nInput Error: Unable to read input. Operation cancelled.");
                return null;
            }
        }
    }
}
