package com.joysistvi.clothingretailapp.repository;
import com.joysistvi.clothingretailapp.model.StockMovement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

// Movement records are added whenever stock changes.
// No update or delete because these serve as our inventory audit history.
public interface StockMovementRepository {
    int insert(Connection connection, StockMovement movement) throws SQLException;
    StockMovement findById(Connection connection, int movementId) throws SQLException;
    List<StockMovement> findByVariantId(Connection connection, int variantId) throws SQLException;
}
