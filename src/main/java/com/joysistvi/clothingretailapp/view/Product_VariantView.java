package com.joysistvi.clothingretailapp.view;

import com.joysistvi.clothingretailapp.model.Product_Variant;
import com.joysistvi.clothingretailapp.model.Products;
import com.joysistvi.clothingretailapp.service.Product_VariantService;
import com.joysistvi.clothingretailapp.service.ProductsService;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import static com.joysistvi.clothingretailapp.view.ConsoleOutput.clearScreen;
import static com.joysistvi.clothingretailapp.view.ConsoleOutput.truncate;

public class Product_VariantView {

    private final Product_VariantService variantService;
    private final ProductsService productsService;
    private final Scanner scanner;

    // Keeps the original constructor available for manual tests.
    public Product_VariantView(Product_VariantService variantService, Scanner scanner) {
        this(variantService, null, scanner);
    }

    // Uses the products service to display names in the cashier catalog.
    public Product_VariantView(Product_VariantService variantService,
                               ProductsService productsService, Scanner scanner) {
        this.variantService = variantService;
        this.productsService = productsService;
        this.scanner = scanner;
    }

    // Displays and handles the product variant menu.
    public void handleMenu() {
        while (true) {
            clearScreen();
            System.out.println("\n=== PRODUCT CATALOG ===");
            System.out.println("1. Browse All Items");
            System.out.println("2. Search by Product, SKU, Size or Color");
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

    // Displays the available product variants.
    private void displayCatalog() {
        ConsoleOutput.operationHeader("BROWSE PRODUCT CATALOG");
        List<Product_Variant> variants = variantService.getAllActiveVariants();
        printCatalog(variants, "Available Items");
    }

    // Prompts the user to search.
    private void promptSearch() {
        ConsoleOutput.operationHeader("SEARCH PRODUCT CATALOG");
        String keyword = getStringInput("Enter product name, SKU, size or color: ");
        if (keyword == null) {
            return;
        }

        String search = keyword.trim().toLowerCase();
        List<Product_Variant> matches = new ArrayList<>();
        for (Product_Variant variant : variantService.getAllActiveVariants()) {
            boolean sizeMatches = variant.getSize() != null && variant.getSize().toLowerCase().contains(search);
            boolean colorMatches = variant.getColor() != null && variant.getColor().toLowerCase().contains(search);
            boolean skuMatches = variant.getSku() != null && variant.getSku().toLowerCase().contains(search);
            boolean productMatches = productName(variant.getProduct_id()).toLowerCase().contains(search);
            if (sizeMatches || colorMatches || skuMatches || productMatches) {
                matches.add(variant);
            }
        }
        printCatalog(matches, "Search Results");
    }

    // Prints the variant ID, product name and stock needed for checkout.
    private void printCatalog(List<Product_Variant> variants, String title) {
        System.out.println("\n----- " + title + " -----");
        System.out.printf("%-7s %-13s %-19s %-8s %-11s %-9s %-7s %-12s%n",
                "ID", "SKU", "Product", "Size", "Color", "Price", "Stock", "Availability");

        if (variants == null || variants.isEmpty()) {
            System.out.println("No items found.");
            return;
        }

        for (Product_Variant variant : variants) {
            System.out.printf("%-7d %-13s %-19s %-8s %-11s %-9.2f %-7d %-12s%n",
                    variant.getVariant_id(), truncate(variant.getSku(), 13),
                    truncate(productName(variant.getProduct_id()), 19),
                    truncate(variant.getSize(), 8), truncate(variant.getColor(), 11),
                    variant.getUnit_price(), variant.getStock_quantity(),
                    variant.getStock_quantity() > 0 ? "In stock" : "Out of stock");
        }
    }

    // Resolves the current product name without changing variant data.
    private String productName(int productId) {
        if (productsService == null) return "Product #" + productId;
        Products product = productsService.findById(productId);
        return product == null ? "Product #" + productId : product.getName();
    }

    // Repeatedly requests a valid whole number.
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

    // Requests nonblank console text.
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
