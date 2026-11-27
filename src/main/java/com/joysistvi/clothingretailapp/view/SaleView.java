package com.joysistvi.clothingretailapp.view;
import com.joysistvi.clothingretailapp.service.SaleService;
import com.joysistvi.clothingretailapp.model.Sale;
import com.joysistvi.clothingretailapp.model.SaleReceipt;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class SaleView {
    private final SaleService sales;
    private final SaleItemView itemView;
    private final ConsoleInput input;
    private final Scanner scanner;

    public SaleView(SaleService sales, SaleItemView itemView, Scanner scanner) {
        this.sales = sales; this.itemView = itemView;
        this.input = new ConsoleInput(scanner); this.scanner = scanner;
    }

    // The authenticated user ID must come from the login session, never from this menu.
    public void handleMenu(int authenticatedUserId) {
        while (true) {
            System.out.println("\n=== SALES / CHECKOUT ===");
            System.out.println("1. New cash sale");
            System.out.println("2. Sales history");
            System.out.println("3. Reprint receipt");
            System.out.println("0. Back");
            try {
                int choice = input.integer("Select: ");
                switch (choice) {
                    case 1 -> checkout(authenticatedUserId);
                    case 2 -> history(authenticatedUserId);
                    case 3 -> reprint(authenticatedUserId);
                    case 0 -> { return; }
                    default -> System.out.println("Invalid option.");
                }
                if (!ConsoleOutput.pause(scanner)) return;
                continue;
            } catch (java.util.NoSuchElementException e) {
                return;
            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Operation failed: " + e.getMessage());
            }

            if (!ConsoleOutput.pause(scanner)) return;
        }
    }

    private void checkout(int userId) throws SQLException {
        Map<Integer, Integer> cart = new LinkedHashMap<>();
        System.out.println("Enter each variant's ID and quantity (0 to finish).");
        while (true) {
            int variant = input.integer("Variant ID (0 to finish): ");
            if (variant == 0) break;
            if (variant < 0) throw new IllegalArgumentException("Variant ID must be positive");
            int quantity = input.positive("Quantity: ");
            // One row per variant, matching UNIQUE(sale_id, variant_id).
            cart.merge(variant, quantity, Math::addExact);
        }
        if (cart.isEmpty()) { System.out.println("Checkout cancelled: empty cart."); return; }
        var cash = input.money("Cash received: ");
        SaleReceipt receipt = sales.checkout(userId, cart, cash);
        try { displayReceipt(receipt); }
        catch (RuntimeException e) { System.out.println("Sale saved as #" + receipt.getSale().getSaleId() + ". Reprint receipt later."); }
    }

    private void history(int userId) throws SQLException {
        var records = sales.history(userId);
        if (records.isEmpty()) { System.out.println("No completed sales found."); return; }
        System.out.printf("%-8s %-10s %-20s %12s %12s%n", "Sale ID", "Cashier", "Date", "Total", "Status");
        for (Sale sale : records) {
            System.out.printf("%-8d %-10d %-20s %12s %12s%n", sale.getSaleId(), sale.getUserId(),
                    sale.getSaleDatetime(), sale.getTotalAmount(), sale.getStatus());
        }
    }

    private void reprint(int userId) throws SQLException {
        int saleId = input.positive("Sale ID: ");
        displayReceipt(sales.receipt(userId, saleId));
    }

    private void displayReceipt(SaleReceipt receipt) {
        Sale sale = receipt.getSale();
        System.out.println("\n----- CIMS-POS RECEIPT -----");
        System.out.println("Sale: #" + sale.getSaleId() + " | Cashier ID: " + sale.getUserId());
        System.out.println("Date: " + sale.getSaleDatetime());
        itemView.display(receipt.getItems());
        System.out.println("Total: " + sale.getTotalAmount());
        System.out.println("Cash: " + sale.getCashReceived());
        System.out.println("Change: " + sale.getChangeAmount());
        System.out.println("Status: " + sale.getStatus());
    }
}
