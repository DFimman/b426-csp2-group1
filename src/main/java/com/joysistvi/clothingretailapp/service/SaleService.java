package com.joysistvi.clothingretailapp.service;
import com.joysistvi.clothingretailapp.model.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

// Create is done through checkout so the sale, items, and stock changes are saved together.
// No update or delete because completed sales are kept for receipts and reports.
public interface SaleService {
    // userId must come from the authenticated session, never raw user input.
    SaleReceipt checkout(int userId, Map<Integer, Integer> cart, BigDecimal cash) throws SQLException;
    SaleReceipt receipt(int actorUserId, int saleId) throws SQLException;
    List<Sale> history(int actorUserId) throws SQLException;
}
