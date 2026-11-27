package com.joysistvi.clothingretailapp.service;

import com.joysistvi.clothingretailapp.repository.*;
import com.joysistvi.clothingretailapp.config.DbConnection;
import com.joysistvi.clothingretailapp.model.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class SaleServiceImpl implements SaleService {
    private final DbConnection db;
    private final SaleRepository sales;
    private final SaleItemRepository items;
    private final StockMovementRepository movements;
    private final TransactionGuardRepository guard;

    public SaleServiceImpl(DbConnection db, SaleRepository sales, SaleItemRepository items,
                           StockMovementRepository movements, TransactionGuardRepository guard) {
        this.db = db; this.sales = sales; this.items = items;
        this.movements = movements; this.guard = guard;
    }


    // Validates and completes a cash sale transaction.
    @Override
    public SaleReceipt checkout(int userId, Map<Integer, Integer> cart, BigDecimal cash) throws SQLException {

        // Check if the cashier ID and shopping cart are valid.
        if (userId <= 0 || cart == null || cart.isEmpty())
            throw new IllegalArgumentException("Valid user and nonempty cart required");

        // Validate the cash amount and format it to two decimal places.
        BigDecimal received = SaleTotals.money(cash);

        // Check that every variant ID and quantity in the cart is positive.
        for (var entry : cart.entrySet()) {
            if (entry.getKey() == null || entry.getKey() <= 0 || entry.getValue() == null || entry.getValue() <= 0)
                throw new IllegalArgumentException("Variant ID and quantity must be positive");
        }

        try (Connection connection = db.connect()) {

            // Disable auto-commit so all checkout changes are saved together.
            boolean priorAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                // Verify that the cashier has an active user account.
                requireActiveUser(connection, userId);
                List<SaleItem> pending = new ArrayList<>();
                Map<Integer, Integer> remainingStock = new HashMap<>();

                // Validate each product variant before saving the sale.
                for (var entry : cart.entrySet()) {
                    int variantId = entry.getKey(), quantity = entry.getValue();

                    // Find the variant and verify that it and its parents are active.
                    var variant = guard.findActiveVariant(connection, variantId);

                    // Reject missing, archived, or out-of-stock variants.
                    if (variant == null)
                        throw new IllegalArgumentException("Variant " + variantId + " is archived or missing");

                    if (variant.stockQuantity() < quantity)
                        throw new IllegalArgumentException("Insufficient stock for variant " + variantId);

                    // Get the current selling price and calculate the item's subtotal.
                    BigDecimal price = SaleTotals.money(variant.unitPrice());

                    // Prepare the item using its quantity, price, and subtotal.
                    pending.add(new SaleItem(0, 0, variantId, quantity, price, SaleTotals.subtotal(quantity, price)));

                    // Calculate the stock remaining after selling the item.
                    remainingStock.put(variantId, MovementRules.nextStock(variant.stockQuantity(), -quantity));
                }

                // Calculate the total amount and the customer's change.
                BigDecimal total = SaleTotals.total(pending);
                BigDecimal change = SaleTotals.change(received, total);

                // Save the main sale record and retrieve its generated ID.
                int saleId = sales.insert(connection, userId, total, received, change);
                List<SaleItem> savedItems = new ArrayList<>();

                // Save each purchased item and update its inventory.
                for (SaleItem line : pending) {
                    int variantId = line.getVariantId();

                    // Deduct the sold quantity from the variant's stock.
                    guard.updateStock(connection, variantId, remainingStock.get(variantId));

                    // Link the purchased item to the newly created sale.
                    SaleItem saved = new SaleItem(0, saleId, variantId, line.getQuantity(), line.getUnitPrice(), line.getSubtotal());

                    // Save the sale item and retrieve its generated ID.
                    int itemId = items.insert(connection, saved);

                    // Keep the saved item details for generating the receipt.
                    savedItems.add(new SaleItem(itemId, saleId, variantId, line.getQuantity(), line.getUnitPrice(), line.getSubtotal()));

                    // Record the stock deduction in the inventory movement history.
                    movements.insert(connection, new StockMovement(0, variantId, userId, saleId, "SALE",
                            -line.getQuantity(), "Checkout sale #" + saleId, null));
                }

                // Retrieve the completed sale details from the database.
                Sale stored = sales.findById(connection, saleId);

                if (stored == null)
                    throw new SQLException("Cannot retrieve committed sale details");

                // Commit all changes only after the entire checkout succeeds.
                connection.commit();

                // Return the receipt; a later display failure must not undo the sale.
                return new SaleReceipt(stored, savedItems);

            } catch (SQLException | RuntimeException e) {

                // Roll back all uncommitted changes if checkout fails.
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    // Preserve the rollback error without losing the original error.
                    e.addSuppressed(rollbackError);
                }

                // Pass the original error to the calling method.
                throw e;

            } finally {
                // Restore the connection's original auto-commit setting.
                connection.setAutoCommit(priorAutoCommit);
            }
        }
    }


    // Retrieves a completed sale receipt.
    @Override
    public SaleReceipt receipt(int actorUserId, int saleId) throws SQLException {
        if (saleId <= 0) throw new IllegalArgumentException("Sale ID must be positive");
        try (Connection connection = db.connect()) {
            String role = requireActiveUser(connection, actorUserId);
            Sale sale = sales.findById(connection, saleId);
            if (sale == null || !"COMPLETED".equals(sale.getStatus())) throw new IllegalArgumentException("Completed sale not found");
            if (!"ADMIN".equals(role) && sale.getUserId() != actorUserId)
                throw new SecurityException("Cashiers may access only their own sales");
            return new SaleReceipt(sale, items.findBySaleId(connection, saleId));
        }
    }

    // Lists the relevant transaction or movement history.
    @Override
    public List<Sale> history(int actorUserId) throws SQLException {
        try (Connection connection = db.connect()) {
            String role = requireActiveUser(connection, actorUserId);
            return sales.findAll(connection, "ADMIN".equals(role) ? null : actorUserId);
        }
    }

    // Checks that the user has an active account.
    private String requireActiveUser(Connection connection, int userId) throws SQLException {
        if (userId <= 0) throw new SecurityException("Authentication required");
        String role = guard.activeRole(connection, userId);
        if (!"ADMIN".equals(role) && !"USER".equals(role))
            throw new SecurityException("Active ADMIN or USER account required");
        return role;
    }
}
