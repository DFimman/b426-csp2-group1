package com.joysistvi.clothingretailapp.view;

import com.joysistvi.clothingretailapp.model.Product_Variant;
import com.joysistvi.clothingretailapp.service.Product_VariantService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ProductVariantAdminView {
    private final Product_VariantService service;
    private final ConsoleInput input;
    private final Scanner scanner;

    // Uses the existing product variant service for Admin catalog operations.
    public ProductVariantAdminView(Product_VariantService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
        this.input = new ConsoleInput(scanner);
    }

    // Displays Admin operations while keeping permanent deletion off the menu.
    public void handleMenu() {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n===== MANAGE PRODUCT VARIANTS =====");
            System.out.println("1. View Active Variants");
            System.out.println("2. View Archived Variants");
            System.out.println("3. Create Variant");
            System.out.println("4. Update Variant");
            System.out.println("5. Search Variants by SKU");
            System.out.println("6. Archive Variant");
            System.out.println("7. Restore Variant");
            System.out.println("0. Back");
            try {
                int choice = input.integer("Choose an option: ");
                if (choice == 0) return;
                switch (choice) {
                    case 1 -> displayTable(service.getAllActiveVariants(), "Active Variants");
                    case 2 -> displayTable(service.getAllArchivedVariants(), "Archived Variants");
                    case 3 -> createVariant();
                    case 4 -> updateVariant();
                    case 5 -> searchVariants();
                    case 6 -> archiveVariant();
                    case 7 -> restoreVariant();
                    default -> System.out.println("Invalid option. Please try again.");
                }
                if (!ConsoleOutput.pause(scanner)) return;
            } catch (NoSuchElementException | IllegalStateException e) {
                return;
            } catch (NumberFormatException e) {
                System.out.println("Validation Error: Please enter a valid number.");
                if (!ConsoleOutput.pause(scanner)) return;
            } catch (IllegalArgumentException e) {
                System.out.println("Validation Error: " + e.getMessage());
                if (!ConsoleOutput.pause(scanner)) return;
            }
        }
    }

    // Creates a variant with zero starting stock for movement tracking.
    private void createVariant() {
        int productId = input.positive("Product ID: ");
        String sku = input.text("SKU: ");
        String size = input.text("Size: ");
        String color = input.text("Color: ");
        double price = Double.parseDouble(input.text("Unit price: "));
        int reorderLevel = input.integer("Reorder level: ");
        boolean created = service.createVariant(productId, sku, size, color, price, 0, reorderLevel);
        System.out.println(created ? "Variant created successfully. Use Restock to add stock." : "Failed to create variant.");
    }

    // Updates variant details while preserving stock movement records.
    private void updateVariant() {
        displayTable(service.getAllActiveVariants(), "Active Variants");
        int id = input.positive("Variant ID to update: ");
        Product_Variant current = service.findById(id);
        if (current == null) {
            System.out.println("Variant not found.");
            return;
        }
        if (current.isIs_archived()) {
            System.out.println("Restore the variant before editing it.");
            return;
        }
        String product = input.text("Product ID (Enter to keep " + current.getProduct_id() + "): ");
        String sku = input.text("SKU (Enter to keep " + current.getSku() + "): ");
        String size = input.text("Size (Enter to keep " + current.getSize() + "): ");
        String color = input.text("Color (Enter to keep " + current.getColor() + "): ");
        String price = input.text("Unit price (Enter to keep " + current.getUnit_price() + "): ");
        String reorder = input.text("Reorder level (Enter to keep " + current.getReorder_level() + "): ");
        boolean updated = service.updateVariant(id,
                product.isBlank() ? current.getProduct_id() : Integer.parseInt(product),
                sku.isBlank() ? current.getSku() : sku,
                size.isBlank() ? current.getSize() : size,
                color.isBlank() ? current.getColor() : color,
                price.isBlank() ? current.getUnit_price() : Double.parseDouble(price),
                current.getStock_quantity(),
                reorder.isBlank() ? current.getReorder_level() : Integer.parseInt(reorder));
        System.out.println(updated ? "Variant updated successfully." : "Failed to update variant.");
    }

    // Searches variants by their unique SKU.
    private void searchVariants() {
        String keyword = input.text("SKU keyword: ");
        displayTable(service.searchVariants(keyword), "Search Results");
    }

    // Archives a variant without deleting its sales history.
    private void archiveVariant() {
        displayTable(service.getAllActiveVariants(), "Active Variants");
        int id = input.positive("Variant ID to archive: ");
        Product_Variant current = service.findById(id);
        if (current == null || current.isIs_archived()) {
            System.out.println("Variant is missing or already archived.");
            return;
        }
        boolean archived = service.archiveVariant(id);
        System.out.println(archived ? "Variant archived successfully." : "Failed to archive variant.");
    }

    // Restores an archived variant only when its parent is usable.
    private void restoreVariant() {
        displayTable(service.getAllArchivedVariants(), "Archived Variants");
        int id = input.positive("Variant ID to restore: ");
        boolean restored = service.restoreVariant(id);
        System.out.println(restored ? "Variant restored successfully." : "Failed to restore variant.");
    }

    // Lists the active variants at or below their reorder levels.
    public void displayLowStock() {
        displayTable(service.getLowStockVariants(), "Low-Stock Alerts");
    }

    // Shows variant IDs and SKUs needed during checkout and restocking.
    private void displayTable(List<Product_Variant> variants, String title) {
        System.out.println("\n----- " + title + " -----");
        System.out.printf("%-7s %-14s %-8s %-8s %-12s %-10s %-8s %-8s%n",
                "ID", "SKU", "Product", "Size", "Color", "Price", "Stock", "Reorder");
        if (variants == null || variants.isEmpty()) {
            System.out.println("No variants found.");
            return;
        }
        for (Product_Variant variant : variants) {
            System.out.printf("%-7d %-14s %-8d %-8s %-12s %-10.2f %-8d %-8d%n",
                    variant.getVariant_id(), ConsoleOutput.truncate(variant.getSku(), 14),
                    variant.getProduct_id(), ConsoleOutput.truncate(variant.getSize(), 8),
                    ConsoleOutput.truncate(variant.getColor(), 12), variant.getUnit_price(),
                    variant.getStock_quantity(), variant.getReorder_level());
        }
    }
}
