package com.joysistvi.clothingretailapp.Repository;
import com.joysistvi.clothingretailapp.Controller.Product_Variant;
import java.util.List;
public interface Product_VariantRepository {

    public Product_Variant findById(int variant_id);
    public Product_Variant findBySku(String sku);
    public List<Product_Variant> findByProductId(int product_id);
    public List<Product_Variant> findAllActive();
    public List<Product_Variant> findAllArchived();
    public List<Product_Variant> findLowStock();
    public boolean save(Product_Variant variant);
    public boolean update(Product_Variant variant);
    public boolean updateStock(int variant_id, int stock_quantity);
    public boolean deleteById(int variant_id);
    public boolean archiveById(int variant_id);
    public boolean restoreById(int variant_id);
    public List<Product_Variant> searchBySku(String keyword);

}
