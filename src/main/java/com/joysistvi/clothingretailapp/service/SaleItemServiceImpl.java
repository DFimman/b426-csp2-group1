package com.joysistvi.clothingretailapp.service;
import com.joysistvi.clothingretailapp.model.SaleItem;
import java.sql.SQLException;
import java.util.List;

public class SaleItemServiceImpl implements SaleItemService {
    private final SaleService sales;
    public SaleItemServiceImpl(SaleService sales) { this.sales = sales; }
    @Override
    public List<SaleItem> findBySaleId(int authenticatedUserId, int saleId) throws SQLException {
        return sales.receipt(authenticatedUserId, saleId).getItems();
    }
}
