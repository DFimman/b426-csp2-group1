package com.joysistvi.clothingretailapp.repository;
import com.joysistvi.clothingretailapp.model.Sale;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleRepositoryImpl implements SaleRepository {
    @Override
    public int insert(Connection connection, int userId, BigDecimal total, BigDecimal cash, BigDecimal change) throws SQLException {
        String sql = "INSERT INTO sales (user_id, total_amount, cash_received, change_amount, status) "
                   + "VALUES (?, ?, ?, ?, 'COMPLETED')";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setBigDecimal(2, total);
            ps.setBigDecimal(3, cash);
            ps.setBigDecimal(4, change);
            if (ps.executeUpdate() != 1) throw new SQLException("Failed to create sale");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Missing generated sale ID");
                return keys.getInt(1);
            }
        }
    }

    @Override
    public Sale findById(Connection connection, int saleId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM sales WHERE sale_id = ?")) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Sale> findAll(Connection connection, Integer userIdFilter) throws SQLException {
        String sql = "SELECT * FROM sales" + (userIdFilter == null ? "" : " WHERE user_id = ?")
                + " ORDER BY sale_datetime DESC, sale_id DESC";
        List<Sale> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (userIdFilter != null) ps.setInt(1, userIdFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        }
        return result;
    }

    private Sale mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("sale_datetime");
        return new Sale(rs.getInt("sale_id"), rs.getInt("user_id"), timestamp.toLocalDateTime(),
                rs.getBigDecimal("total_amount"), rs.getBigDecimal("cash_received"),
                rs.getBigDecimal("change_amount"), rs.getString("status"));
    }
}
