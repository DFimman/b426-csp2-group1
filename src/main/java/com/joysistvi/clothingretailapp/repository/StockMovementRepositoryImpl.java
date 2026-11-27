package com.joysistvi.clothingretailapp.repository;
import com.joysistvi.clothingretailapp.model.StockMovement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockMovementRepositoryImpl implements StockMovementRepository {
    // Inserts a new stock movement record.
    @Override
    public int insert(Connection connection, StockMovement movement) throws SQLException {
        String sql = "INSERT INTO stock_movements (variant_id, user_id, sale_id, movement_type, quantity_change, reason) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, movement.getVariantId());
            ps.setInt(2, movement.getUserId());
            if (movement.getSaleId() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, movement.getSaleId());
            ps.setString(4, movement.getMovementType());
            ps.setInt(5, movement.getQuantityChange());
            ps.setString(6, movement.getReason());
            if (ps.executeUpdate() != 1) throw new SQLException("Failed to record stock movement");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Missing generated movement ID");
                return keys.getInt(1);
            }
        }
    }

    @Override
    public StockMovement findById(Connection connection, int movementId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM stock_movements WHERE movement_id = ?")) {
            ps.setInt(1, movementId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapRow(rs) : null; }
        }
    }

    // Converts one database row to a stock movement object.
    private StockMovement mapRow(ResultSet rs) throws SQLException {
        int saleId = rs.getInt("sale_id");
        Integer linkedSale = rs.wasNull() ? null : saleId;
        return new StockMovement(rs.getInt("movement_id"), rs.getInt("variant_id"),
                rs.getInt("user_id"), linkedSale, rs.getString("movement_type"),
                rs.getInt("quantity_change"), rs.getString("reason"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    // Retrieves records for the specified variant.
    @Override
    public List<StockMovement> findByVariantId(Connection connection, int variantId) throws SQLException {
        List<StockMovement> result = new ArrayList<>();
        String sql = "SELECT * FROM stock_movements WHERE variant_id = ? ORDER BY created_at DESC, movement_id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }
}
