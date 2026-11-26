package com.joysistvi.clothingretailapp.repository;
import java.math.BigDecimal;
import java.sql.*;

// repository-only SQL shared by stock and checkout services.
public class TransactionGuardRepository {
    public record LockedVariant(BigDecimal unitPrice, int stockQuantity) {}

    public String activeRole(Connection connection, int userId) throws SQLException {
        String sql = "SELECT role FROM users WHERE user_id = ? AND is_archived = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("role") : null;
            }
        }
    }

    // Includes the product and category archive gates. Lock rows through checkout.
    public LockedVariant lockActiveVariant(Connection connection, int variantId) throws SQLException {
        String sql = "SELECT v.unit_price, v.stock_quantity FROM product_variants v "
            + "JOIN products p ON p.product_id = v.product_id "
            + "JOIN categories c ON c.category_id = p.category_id "
            + "WHERE v.variant_id = ? AND v.is_archived = 0 "
            + "AND p.is_archived = 0 AND c.is_archived = 0 FOR UPDATE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new LockedVariant(rs.getBigDecimal("unit_price"), rs.getInt("stock_quantity")) : null;
            }
        }
    }

    // Conditional update prevents stale writes if a concurrent operation changes stock.
    public void changeStock(Connection connection, int variantId, int oldQuantity, int newQuantity) throws SQLException {
        String sql = "UPDATE product_variants SET stock_quantity = ? "
            + "WHERE variant_id = ? AND stock_quantity = ? AND is_archived = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, variantId);
            ps.setInt(3, oldQuantity);
            if (ps.executeUpdate() != 1) throw new SQLException("Stock changed during transaction");
        }
    }
}
