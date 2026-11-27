package com.joysistvi.clothingretailapp.repository;
import com.joysistvi.clothingretailapp.model.SaleItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleItemRepositoryImpl implements SaleItemRepository {

    // Inserts a new sale item record.
    @Override
    public int insert(Connection connection, SaleItem item) throws SQLException {
        String sql = "INSERT INTO sale_items (sale_id, variant_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getSaleId());
            ps.setInt(2, item.getVariantId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.setBigDecimal(5, item.getSubtotal());
            if (ps.executeUpdate() != 1) throw new SQLException("Failed to save sale item");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Missing generated sale item ID");
                return keys.getInt(1);
            }
        }
    }

    // Finds sale items belonging to a sale.
    @Override
    public List<SaleItem> findBySaleId(Connection connection, int saleId) throws SQLException {
        List<SaleItem> result = new ArrayList<>();
        String sql = "SELECT * FROM sale_items WHERE sale_id = ? ORDER BY sale_item_id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new SaleItem(rs.getInt("sale_item_id"), rs.getInt("sale_id"),
                        rs.getInt("variant_id"), rs.getInt("quantity"),
                        rs.getBigDecimal("unit_price"), rs.getBigDecimal("subtotal")));
            }
        }
        return result;
    }
}
