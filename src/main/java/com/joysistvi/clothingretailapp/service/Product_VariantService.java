package com.joysistvi.clothingretailapp.service;

import com.joysistvi.clothingretailapp.model.Product_Variant;
import java.util.List;

public interface Product_VariantService {

    public List<Product_Variant> getAllActiveVariants();
    public List<Product_Variant> getAllArchivedVariants();
    public boolean createVariant(int product_id, String sku, String size, String color, double unit_prize, int stock_quantity, int reorder_level);
    public boolean updateVariant(int variant_id, int product_id, String sku, String size, String color, double unit_prize, int stock_quantity, int reorder_level);
    public boolean deleteVariant(int variant_id);
    public boolean archiveVariant(int variant_id);
    public boolean restoreVariant(int variant_id);
    public List<Product_Variant> searchVariants(String keyword);

    Product_Variant findById(int variant_id);
    Product_Variant findBySku(String sku);
    List<Product_Variant> getVariantsByProduct(int product_id);
    List<Product_Variant> getLowStockVariants();
    boolean updateStock(int variant_id, int stock_quantity);
}