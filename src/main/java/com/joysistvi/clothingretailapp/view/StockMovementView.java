package com.joysistvi.clothingretailapp.view;
import com.joysistvi.clothingretailapp.service.StockMovementService;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class StockMovementView {
    private final StockMovementService service;
    private final ConsoleInput input;
    private final Scanner scanner;

    public StockMovementView(StockMovementService service, Scanner scanner) {
        this.service = service; this.scanner = scanner;
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
}
