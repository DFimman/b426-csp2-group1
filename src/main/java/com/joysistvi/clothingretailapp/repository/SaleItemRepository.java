package com.joysistvi.clothingretailapp.repository;
import com.joysistvi.clothingretailapp.model.SaleItem;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

// Sale items are inserted together with their sale, not through a separate CRUD menu.
// We only read them after checkout so saved purchase details are not changed.
public interface SaleItemRepository {
    int insert(Connection connection, SaleItem item) throws SQLException;
    List<SaleItem> findBySaleId(Connection connection, int saleId) throws SQLException;
}
