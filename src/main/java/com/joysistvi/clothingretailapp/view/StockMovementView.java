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

    public void handleMenu(int authenticatedAdminId) {
        while (true) {
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
            } catch (NoSuchElementException e) { return; }
              catch (SQLException | RuntimeException e) { System.out.println("Operation failed: " + e.getMessage()); }
        }
    }

    private void change(int adminId, String type) throws SQLException {
        int variantId = input.positive("Variant ID: ");
        int delta = "RESTOCK".equals(type) ? input.positive("Units to add: ") : input.integer("Signed change (+/-): ");
        String reason = input.text("Reason: ");
        var movement = service.recordManualChange(adminId, variantId, type, delta, reason);
        System.out.println(type + " saved. Movement #" + movement.getMovementId());
    }

    private void history(int adminId) throws SQLException {
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
