package com.joysistvi.clothingretailapp.service;

import com.joysistvi.clothingretailapp.repository.StockMovementRepository;
import com.joysistvi.clothingretailapp.repository.TransactionGuardRepository;
import com.joysistvi.clothingretailapp.config.DbConnection;
import com.joysistvi.clothingretailapp.model.StockMovement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StockMovementServiceImpl implements StockMovementService {
    private final DbConnection db;
    private final StockMovementRepository movements;
    private final TransactionGuardRepository guard;

    public StockMovementServiceImpl(DbConnection db, StockMovementRepository movements, TransactionGuardRepository guard) {
        this.db = db; this.movements = movements; this.guard = guard;
    }

    // Records and audits a manual stock change.
    @Override
    public StockMovement recordManualChange(int actorId, int variantId, String type, int delta, String reason) throws SQLException {
        if (variantId <= 0) throw new IllegalArgumentException("Variant ID must be positive");
        MovementRules.validateManual(type, delta, reason);
        try (Connection connection = db.connect()) {
            boolean priorAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                requireAdmin(connection, actorId);
                var variant = guard.findActiveVariant(connection, variantId);
                if (variant == null) throw new IllegalArgumentException("Variant or its parent is archived or missing");
                int updatedStock = MovementRules.nextStock(variant.stockQuantity(), delta);
                guard.updateStock(connection, variantId, updatedStock);
                var movement = new StockMovement(0, variantId, actorId, null, type, delta, reason.trim(), null);
                int id = movements.insert(connection, movement);
                StockMovement stored = movements.findById(connection, id);
                if (stored == null) throw new SQLException("Cannot retrieve recorded stock movement");
                connection.commit();
                return stored;
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            } finally {
                connection.setAutoCommit(priorAutoCommit);
            }
        }
    }

    // Lists the relevant transaction or movement history.
    @Override
    public List<StockMovement> history(int actorId, int variantId) throws SQLException {
        if (variantId <= 0) throw new IllegalArgumentException("Variant ID must be positive");
        try (Connection connection = db.connect()) {
            requireAdmin(connection, actorId);
            return movements.findByVariantId(connection, variantId);
        }
    }

    // Checks that the current user has Admin access.
    private void requireAdmin(Connection connection, int actorId) throws SQLException {
        if (actorId <= 0 || !"ADMIN".equals(guard.activeRole(connection, actorId)))
            throw new SecurityException("Only active admins can change or inspect stock movements");
    }
}
