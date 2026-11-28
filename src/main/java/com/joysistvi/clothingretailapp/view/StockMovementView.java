package com.joysistvi.clothingretailapp.view;
import com.joysistvi.clothingretailapp.service.StockMovementService;
import com.joysistvi.clothingretailapp.service.Product_VariantService;
import com.joysistvi.clothingretailapp.model.Product_Variant;
import java.util.List;
import java.util.Comparator;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class StockMovementView {
    private final StockMovementService service;
    private final Product_VariantService variants;
    private final ConsoleInput input;
    private final Scanner scanner;

    public StockMovementView(StockMovementService service, Scanner scanner) {
        this(service, null, scanner);
    }

    // Uses the active variant catalog to display stocks before editing inventory.
    public StockMovementView(StockMovementService service, Product_VariantService variants, Scanner scanner) {
        this.service = service;
        this.variants = variants;
        this.scanner = scanner;
        this.input = new ConsoleInput(scanner);
    }

    // Displays and handles the stock movement menu.
    public void handleMenu(int authenticatedAdminId) {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n=== STOCK MOVEMENTS (ADMIN) ===");
            System.out.println("1. Restock variant");
            System.out.println("2. Adjust variant stock (+/-)");
            System.out.println("3. View movement history");
            System.out.println("0. Back");
            try {
                int choice = input.integer("Select: ");
                switch (choice) {
                    case 1 -> change(authenticatedAdminId, "RESTOCK");
                    case 2 -> change(authenticatedAdminId, "ADJUSTMENT");
                    case 3 -> history(authenticatedAdminId);
                    case 0 -> { return; }
                    default -> System.out.println("Invalid option.");
                }
                if (!ConsoleOutput.pause(scanner)) return;
                continue;
            } catch (NoSuchElementException e) {
                return;
            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Operation failed: " + e.getMessage());
            }
            if (!ConsoleOutput.pause(scanner)) return;
        }
    }

    // Prompts for and records a manual stock change.
    private void change(int adminId, String type) throws SQLException {
        ConsoleOutput.operationHeader("RESTOCK".equals(type) ? "RESTOCK VARIANT" : "ADJUST VARIANT STOCK");
        // Let the Admin see variant IDs and stock levels before choosing one.
        if (!printActiveStocks()) return;
        int variantId = input.positive("Variant ID: ");
        int delta = "RESTOCK".equals(type) ? input.positive("Units to add: ") : input.integer("Signed change (+/-): ");
        String reason = input.text("Reason: ");
        var movement = service.recordManualChange(adminId, variantId, type, delta, reason);
        System.out.println(type + " saved. Movement #" + movement.getMovementId());
    }

    // Lists the relevant transaction or movement history.
    private void history(int adminId) throws SQLException {
        ConsoleOutput.operationHeader("STOCK MOVEMENT HISTORY");
        int variantId = input.positive("Variant ID: ");
        var records = service.history(adminId, variantId);
        if (records.isEmpty()) { System.out.println("No movements found."); return; }
        System.out.printf("%-7s %-12s %8s %-10s %-20s %s%n", "ID", "Type", "Change", "Sale", "Date", "Reason");
        for (var movement : records) {
            System.out.printf("%-7d %-12s %8d %-10s %-20s %s%n", movement.getMovementId(),
                    movement.getMovementType(), movement.getQuantityChange(),
                    movement.getSaleId() == null ? "-" : movement.getSaleId().toString(),
                    movement.getCreatedAt(), movement.getReason());
        }
    }

    // Displays the active inventory without requiring a stock movement.
    public void displayStocks() {
        ConsoleOutput.operationHeader("VIEW STOCKS");
        printActiveStocks();
    }

    // Prints active variants with their SKU, quantity, and reorder threshold.
    private boolean printActiveStocks() {
        if (variants == null) {
            System.out.println("Variant stock list is unavailable.");
            return true; // Keep the older manual-test constructor compatible.
        }
        List<Product_Variant> active = variants.getAllActiveVariants();
        System.out.println("\n----- Active Variant Stocks -----");
        System.out.printf("%-7s %-16s %-9s %-12s %-10s %-9s%n",
                "ID", "SKU", "Size", "Color", "Stock", "Reorder");
        if (active == null || active.isEmpty()) {
            System.out.println("No active variants available.");
            return false;
        }
        for (Product_Variant variant : active) {
            System.out.printf("%-7d %-16s %-9s %-12s %-10d %-9d%n",
                    variant.getVariant_id(), ConsoleOutput.truncate(variant.getSku(), 16),
                    ConsoleOutput.truncate(variant.getSize(), 9),
                    ConsoleOutput.truncate(variant.getColor(), 12),
                    variant.getStock_quantity(), variant.getReorder_level());
        }
        return true;
    }
}
