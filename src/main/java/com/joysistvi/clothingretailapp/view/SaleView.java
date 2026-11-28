package com.joysistvi.clothingretailapp.view;
import com.joysistvi.clothingretailapp.service.SaleService;
import com.joysistvi.clothingretailapp.service.UserService;
import com.joysistvi.clothingretailapp.model.User;
import java.math.BigDecimal;
import com.joysistvi.clothingretailapp.model.Sale;
import com.joysistvi.clothingretailapp.model.SaleReceipt;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.Scanner;

public class SaleView {
    private final SaleService sales;
    private final UserService users;
    private final SaleItemView itemView;
    private final ConsoleInput input;
    private final Scanner scanner;
    private final Map<Integer, Integer> pendingCart = new LinkedHashMap<>();

    // Initializes SaleView with its required dependencies.
    public SaleView(SaleService sales, SaleItemView itemView, Scanner scanner) {
        this(sales, itemView, null, scanner);
    }

    // Resolves cashier usernames when showing sale history.
    public SaleView(SaleService sales, SaleItemView itemView, UserService users, Scanner scanner) {
        this.sales = sales;
        this.users = users;
        this.itemView = itemView;
        this.input = new ConsoleInput(scanner);
        this.scanner = scanner;
    }

    // The authenticated user ID must come from the login session, never from this menu.
    public void handleMenu(int authenticatedUserId) {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n=== SALES / CHECKOUT ===");
            System.out.println("1. New cash sale");
            System.out.println("2. Sales history");
            System.out.println("3. Reprint receipt");
            System.out.println("0. Back");
            try {
                int choice = input.integer("Select: ");
                switch (choice) {
                    case 1 -> checkout(authenticatedUserId);
                    case 2 -> history(authenticatedUserId, false);
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

    // Validates and completes a cash sale transaction.
    private void checkout(int userId) throws SQLException {
        ConsoleOutput.operationHeader("NEW CASH SALE");
        System.out.println("Enter \"0\" to continue to cash payment, or to exit with an empty cart.");
        Map<Integer, Integer> cart = new LinkedHashMap<>();
        while (true) {
            int variant = promptVariantId();
            if (variant == 0) break;
            int quantity = promptQuantity();
            // Keep one sale line per variant and prevent integer overflow.
            try {
                cart.merge(variant, quantity, Math::addExact);
            } catch (ArithmeticException e) {
                System.out.println("Invalid input. Please try again. The quantity is too large.");
            }
        }
        if (cart.isEmpty()) {
            System.out.println("Checkout cancelled: empty cart.");
            return;
        }
        while (true) {
            BigDecimal cash = promptCashReceived();
            if (cash.signum() == 0) {
                System.out.println("Checkout cancelled.");
                return;
            }
            try {
                SaleReceipt receipt = sales.checkout(userId, cart, cash);
                try { displayReceipt(receipt); }
                catch (RuntimeException e) {
                    System.out.println("Sale saved as #" + receipt.getSale().getSaleId() + ". Reprint receipt later.");
                }
                return;
            } catch (IllegalArgumentException e) {
                // Allow the cashier to retry the payment without discarding the cart.
                System.out.println("Invalid input. Please try again: " + e.getMessage());
                System.out.println("Enter a new cash amount to retry, or 0 to cancel.");
                // A cancelled checkout has not changed the database.
                // Insufficient cash is the common recoverable validation error.
            }
            if (cart.isEmpty()) return;
        }
    }

    // Keeps requesting a valid variant ID instead of leaving the sale operation.
    private int promptVariantId() {
        while (true) {
            try {
                int id = Integer.parseInt(input.text("Variant ID (enter 0 to continue/exit): "));
                if (id >= 0) return id;
            } catch (NumberFormatException ignored) {
                // Show the same input message for letters and negative numbers.
            }
            System.out.println("Invalid input. Please try again. Enter a valid variant ID.");
        }
    }

    // Repeats the quantity prompt until a positive whole number is entered.
    private int promptQuantity() {
        while (true) {
            try {
                int quantity = Integer.parseInt(input.text("Quantity: "));
                if (quantity > 0) return quantity;
            } catch (NumberFormatException ignored) {
                // Keep the customer on the same item line.
            }
            System.out.println("Invalid input. Please try again. Enter a positive quantity.");
        }
    }

    // Rejects invalid cash input without leaving the sale operation.
    private BigDecimal promptCashReceived() {
        while (true) {
            try {
                BigDecimal cash = new BigDecimal(input.text("Cash received (0 to cancel): "));
                if (cash.signum() >= 0) return cash;
            } catch (NumberFormatException ignored) {
                // Keep requesting a valid monetary value.
            }
            System.out.println("Invalid input. Please try again. Enter a nonnegative cash amount.");
        }
    }

    // Lists the relevant transaction or movement history.
    private void history(int userId, boolean mySales) throws SQLException {
        ConsoleOutput.operationHeader("SALES HISTORY");
        var records = sales.history(userId);
        if (records.isEmpty()) { System.out.println("No completed sales found."); return; }
        if (mySales) {
            // The logged-in Cashier already knows whose sales are displayed.
            System.out.printf("%-8s %-20s %12s %12s%n", "Sale ID", "Date", "Total", "Status");
            for (Sale sale : records) {
                System.out.printf("%-8d %-20s %12s %12s%n", sale.getSaleId(),
                        sale.getSaleDatetime(), sale.getTotalAmount(), sale.getStatus());
            }
        } else {
            System.out.printf("%-8s %-20s %-20s %12s %12s%n", "Sale ID", "Username", "Date", "Total", "Status");
            for (Sale sale : records) {
                System.out.printf("%-8d %-20s %-20s %12s %12s%n", sale.getSaleId(),
                        ConsoleOutput.truncate(cashierUsername(sale.getUserId()), 20),
                        sale.getSaleDatetime(), sale.getTotalAmount(), sale.getStatus());
            }
        }
    }

    // Resolves the cashier username, retaining a readable fallback for deleted accounts.
    private String cashierUsername(int userId) {
        if (users == null) return "User #" + userId;
        User user = users.findById(userId);
        return user == null ? "User #" + userId : user.getUsername();
    }

    // Reprints a previously saved sale receipt.
    private void reprint(int userId) throws SQLException {
        ConsoleOutput.operationHeader("REPRINT RECEIPT");
        int saleId = input.positive("Sale ID: ");
        displayReceipt(sales.receipt(userId, saleId));
    }

    // Lets the Cashier prepare, review, remove or clear items before checkout.
    public void manageCart() {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n=== MANAGE CART ===");
            printCart();
            System.out.println("1. Add Item");
            System.out.println("2. Remove Item");
            System.out.println("3. Clear Cart");
            System.out.println("0. Back");
            try {
                int choice = input.integer("Select: ");
                switch (choice) {
                    case 1 -> {
                        ConsoleOutput.operationHeader("ADD CART ITEM");
                        int variantId = input.positive("Variant ID: ");
                        int quantity = input.positive("Quantity: ");
                        pendingCart.merge(variantId, quantity, Math::addExact);
                        System.out.println("Item added to cart. Stock will be verified at checkout.");
                    }
                    case 2 -> {
                        ConsoleOutput.operationHeader("REMOVE CART ITEM");
                        int variantId = input.positive("Variant ID to remove: ");
                        System.out.println(pendingCart.remove(variantId) == null
                                ? "Variant was not in the cart." : "Item removed from cart.");
                    }
                    case 3 -> {
                        ConsoleOutput.operationHeader("CLEAR CART");
                        pendingCart.clear();
                        System.out.println("Cart cleared.");
                    }
                    case 0 -> { return; }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (NoSuchElementException | IllegalStateException e) {
                return;
            } catch (RuntimeException e) {
                System.out.println("Validation Error: " + e.getMessage());
            }
            // Do not clear the result until the Cashier has read it.
            if (!ConsoleOutput.pause(scanner)) return;
        }
    }

    // Prints the current cart; products are validated again during checkout.
    private void printCart() {
        if (pendingCart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.printf("%-12s %-10s%n", "Variant ID", "Quantity");
        for (var item : pendingCart.entrySet()) {
            System.out.printf("%-12d %-10d%n", item.getKey(), item.getValue());
        }
    }

    // Completes the prepared cart using the authenticated Cashier account.
    public void checkoutCurrentCart(int userId) {
        ConsoleOutput.operationHeader("CASH CHECKOUT");
        if (pendingCart.isEmpty()) {
            System.out.println("Checkout cancelled: add items to your cart first.");
            return;
        }
        try {
            var cash = input.money("Cash received: ");
            SaleReceipt receipt = sales.checkout(userId, new LinkedHashMap<>(pendingCart), cash);
            // A successful checkout is committed, so clear the cart even if receipt printing fails.
            pendingCart.clear();
            try { displayReceipt(receipt); }
            catch (RuntimeException e) {
                System.out.println("Sale saved as #" + receipt.getSale().getSaleId() + ". Reprint receipt later.");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Operation failed: " + e.getMessage());
        }
    }

    // Shows the signed-in Cashier's sales without opening the Sales submenu.
    public void showSalesHistory(int userId) {
        try { history(userId, true); }
        catch (SQLException e) { System.out.println("Database Error: " + e.getMessage()); }
        catch (RuntimeException e) { System.out.println("Operation failed: " + e.getMessage()); }
    }

    // Reprints one saved receipt without opening the Sales submenu.
    public void reprintSavedReceipt(int userId) {
        try { reprint(userId); }
        catch (SQLException e) { System.out.println("Database Error: " + e.getMessage()); }
        catch (RuntimeException e) { System.out.println("Operation failed: " + e.getMessage()); }
    }

    // Prevents unfinished carts from being shared across login sessions.
    public void clearPendingCart() {
        pendingCart.clear();
    }

    // Displays the completed sale receipt.
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
