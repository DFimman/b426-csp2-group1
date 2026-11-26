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

    @Override
    public SaleReceipt checkout(int userId, Map<Integer, Integer> cart, BigDecimal cash) throws SQLException {
        if (userId <= 0 || cart == null || cart.isEmpty()) throw new IllegalArgumentException("Valid user and nonempty cart required");
        BigDecimal received = SaleTotals.money(cash);
        // Validate every cart item before checkout.
        for (var entry : cart.entrySet()) {
            if (entry.getKey() == null || entry.getKey() <= 0 || entry.getValue() == null || entry.getValue() <= 0)
                throw new IllegalArgumentException("Variant ID and quantity must be positive");
        }

        try (Connection connection = db.connect()) {
            boolean priorAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                requireActiveUser(connection, userId);
                List<SaleItem> pending = new ArrayList<>();
                Map<Integer, Integer> remainingStock = new HashMap<>();
                for (var entry : cart.entrySet()) {
                    int variantId = entry.getKey(), quantity = entry.getValue();
                    var variant = guard.findActiveVariant(connection, variantId);
                    if (variant == null) throw new IllegalArgumentException("Variant " + variantId + " is archived or missing");
                    if (variant.stockQuantity() < quantity) throw new IllegalArgumentException("Insufficient stock for variant " + variantId);
                    BigDecimal price = SaleTotals.money(variant.unitPrice());
                    pending.add(new SaleItem(0, 0, variantId, quantity, price, SaleTotals.subtotal(quantity, price)));
                    remainingStock.put(variantId, MovementRules.nextStock(variant.stockQuantity(), -quantity));
                }

                BigDecimal total = SaleTotals.total(pending);
                BigDecimal change = SaleTotals.change(received, total);
                int saleId = sales.insert(connection, userId, total, received, change);
                List<SaleItem> savedItems = new ArrayList<>();
                for (SaleItem line : pending) {
                    int variantId = line.getVariantId();
                    guard.updateStock(connection, variantId, remainingStock.get(variantId));
                    SaleItem saved = new SaleItem(0, saleId, variantId, line.getQuantity(), line.getUnitPrice(), line.getSubtotal());
                    int itemId = items.insert(connection, saved);
                    savedItems.add(new SaleItem(itemId, saleId, variantId, line.getQuantity(), line.getUnitPrice(), line.getSubtotal()));
                    movements.insert(connection, new StockMovement(0, variantId, userId, saleId, "SALE",
                            -line.getQuantity(), "Checkout sale #" + saleId, null));
                }
                Sale stored = sales.findById(connection, saleId);
                if (stored == null) throw new SQLException("Cannot retrieve committed sale details");
                connection.commit();
                // Display failure after commit must NOT undo the sale.
                return new SaleReceipt(stored, savedItems);
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            } finally {
                connection.setAutoCommit(priorAutoCommit);
            }
        }
    }

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

    @Override
    public List<Sale> history(int actorUserId) throws SQLException {
        try (Connection connection = db.connect()) {
            String role = requireActiveUser(connection, actorUserId);
            return sales.findAll(connection, "ADMIN".equals(role) ? null : actorUserId);
        }
    }

    private String requireActiveUser(Connection connection, int userId) throws SQLException {
        if (userId <= 0) throw new SecurityException("Authentication required");
        String role = guard.activeRole(connection, userId);
        if (!"ADMIN".equals(role) && !"USER".equals(role))
            throw new SecurityException("Active ADMIN or USER account required");
        return role;
    }
}
