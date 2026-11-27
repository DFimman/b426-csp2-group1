package com.joysistvi.clothingretailapp.repository;
import java.math.BigDecimal;
import java.sql.*;

// SQL helper for checkout and stock changes.
public class TransactionGuardRepository {
    public record ActiveVariant(BigDecimal unitPrice, int stockQuantity) {}

    public String activeRole(Connection connection, int userId) throws SQLException {
        String sql = "SELECT role FROM users WHERE user_id = ? AND is_archived = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("role") : null;
            }
        }
    }

    // Find active variant and check its product and category.
    public ActiveVariant findActiveVariant(Connection connection, int variantId) throws SQLException {
        String sql = "SELECT v.unit_price, v.stock_quantity FROM product_variants v "
            + "JOIN products p ON p.product_id = v.product_id "
            + "JOIN categories c ON c.category_id = p.category_id "
            + "WHERE v.variant_id = ? AND v.is_archived = 0 "
            + "AND p.is_archived = 0 AND c.is_archived = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new ActiveVariant(rs.getBigDecimal("unit_price"), rs.getInt("stock_quantity")) : null;
            }
        }
    }

    // Update stock after the service validates the available quantity.
    public void updateStock(Connection connection, int variantId, int newQuantity) throws SQLException {
        if (newQuantity < 0) throw new IllegalArgumentException("Stock cannot be negative");
        String sql = "UPDATE product_variants SET stock_quantity = ? WHERE variant_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, variantId);
            if (ps.executeUpdate() != 1) throw new SQLException("Failed to update stock");
        }
    }
}
