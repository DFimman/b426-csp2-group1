package com.joysistvi.clothingretailapp.service;
import com.joysistvi.clothingretailapp.model.SaleItem;
import java.sql.SQLException;
import java.util.List;

// Sale items are created automatically when a sale is completed.
// No separate create, update, or delete because we need to keep the original purchase details.
public interface SaleItemService {
    List<SaleItem> findBySaleId(int authenticatedUserId, int saleId) throws SQLException;
}
