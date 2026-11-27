package com.joysistvi.clothingretailapp.repository;
import com.joysistvi.clothingretailapp.model.Sale;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

// Insert is only used during checkout, with the same connection as sale items and stock.
// We do not update or delete completed sales to keep the transaction history.
public interface SaleRepository {
    int insert(Connection connection, int userId, BigDecimal total, BigDecimal cash, BigDecimal change) throws SQLException;
    Sale findById(Connection connection, int saleId) throws SQLException;
    List<Sale> findAll(Connection connection, Integer userIdFilter) throws SQLException;
}
