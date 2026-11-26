package com.joysistvi.clothingretailapp.service;
import com.joysistvi.clothingretailapp.model.StockMovement;
import java.sql.SQLException;
import java.util.List;

// Stock movements are created when we restock, adjust stock, or complete a sale.
// No update or delete because these records are needed for stock history.
public interface StockMovementService {
    StockMovement recordManualChange(int authenticatedAdminId, int variantId, String type, int delta, String reason) throws SQLException;
    List<StockMovement> history(int authenticatedAdminId, int variantId) throws SQLException;
}
