package com.joysistvi.clothingretailapp.service;

import com.joysistvi.clothingretailapp.model.Product_Variant;
import com.joysistvi.clothingretailapp.model.Products;
import com.joysistvi.clothingretailapp.model.Categories;
import com.joysistvi.clothingretailapp.repository.ProductsRepository;
import com.joysistvi.clothingretailapp.repository.CategoriesRepository;
import com.joysistvi.clothingretailapp.repository.Product_VariantRepository;
import java.util.Collections;
import java.util.List;

public class Product_VariantServiceImpl implements Product_VariantService {
    private final Product_VariantRepository variantRepository;
    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;

    public Product_VariantServiceImpl(Product_VariantRepository variantRepository,
                                      ProductsRepository productsRepository,
                                      CategoriesRepository categoriesRepository) {
        this.variantRepository = variantRepository;
        this.productsRepository = productsRepository;
        this.categoriesRepository = categoriesRepository;
    }

    private boolean isUsableParent(int productId) {
        Products product = productsRepository.findById(productId);
        if (product == null) {
            System.out.println("Validation Error: Product with ID " + productId + " was not found.");
            return false;
        }
        if (product.isArchived()) {
            System.out.println("Validation Error: Product " + productId + " is archived. Restore it first.");
            return false;
        }
        Categories category = categoriesRepository.findById(product.getCategoryId());
        if (category == null) {
            System.out.println("Validation Error: Category with ID " + product.getCategoryId() + " was not found.");
            return false;
        }
        if (category.isArchived()) {
            System.out.println("Validation Error: Category " + product.getCategoryId() + " is archived. Restore it first.");
            return false;
        }
        return true;
    }

    private String validateFields(int productId, String sku, String size, String color,
                                  double unitPrice, int stockQuantity, int reorderLevel) {
        if (productId <= 0) return "Product ID must be greater than 0.";
        if (sku == null || sku.isBlank()) return "SKU is required.";
        if (size == null || size.isBlank()) return "Size is required.";
        if (color == null || color.isBlank()) return "Color is required.";
        if (!Double.isFinite(unitPrice) || unitPrice <= 0) return "Unit price must be positive.";
        if (stockQuantity < 0) return "Stock quantity cannot be negative.";
        if (reorderLevel < 0) return "Reorder level cannot be negative.";
        return null;
    }

    @Override
    public List<Product_Variant> getAllActiveVariants() {
        return variantRepository.findAllActive();
    }

    @Override
    public List<Product_Variant> getAllArchivedVariants() {
        return variantRepository.findAllArchived();
    }

    @Override
    public boolean createVariant(int product_id, String sku, String size, String color,
                                 double unit_prize, int stock_quantity, int reorder_level) {
        String error = validateFields(product_id, sku, size, color, unit_prize, stock_quantity, reorder_level);
        if (error != null) {
            System.out.println("Validation Error: " + error);
            return false;
        }
        // New variants start at zero. Use Stock Movements to record the initial restock.
        if (stock_quantity != 0) {
            System.out.println("Validation Error: Create variants with zero stock, then restock through Stock Movements.");
            return false;
        }
        if (!isUsableParent(product_id)) return false;
        if (variantRepository.findBySku(sku.trim()) != null) {
            System.out.println("Validation Error: SKU is already in use.");
            return false;
        }
        return variantRepository.save(new Product_Variant(product_id, sku.trim(), size.trim(),
                color.trim(), unit_prize, stock_quantity, reorder_level));
    }

    @Override
    public boolean updateVariant(int variant_id, int product_id, String sku, String size,
                                 String color, double unit_prize, int stock_quantity, int reorder_level) {
        if (variant_id <= 0) {
            System.out.println("Validation Error: Variant ID must be greater than 0.");
            return false;
        }
        String error = validateFields(product_id, sku, size, color, unit_prize, stock_quantity, reorder_level);
        if (error != null) {
            System.out.println("Validation Error: " + error);
            return false;
        }
        Product_Variant variant = variantRepository.findById(variant_id);
        if (variant == null) {
            System.out.println("Validation Error: Variant with ID " + variant_id + " was not found.");
            return false;
        }
        if (!isUsableParent(product_id)) return false;
        // Block changes that would bypass the stock movement audit trail.
        if (stock_quantity != variant.getStock_quantity()) {
            System.out.println("Validation Error: Change stock through Stock Movements.");
            return false;
        }
        Product_Variant sameSku = variantRepository.findBySku(sku.trim());
        if (sameSku != null && sameSku.getVariant_id() != variant_id) {
            System.out.println("Validation Error: SKU is already in use.");
            return false;
        }
        variant.setProduct_id(product_id);
        variant.setSku(sku.trim());
        variant.setSize(size.trim());
        variant.setColor(color.trim());
        variant.setUnit_price(unit_prize);
        // Keep the existing stock. Only StockMovementService should change it.
        variant.setReorder_level(reorder_level);
        return variantRepository.update(variant);
    }

    // Kept for maintenance. Do not permanently delete variants during normal operation.
    @Override
    public boolean deleteVariant(int variant_id) {
        if (variant_id <= 0) {
            System.out.println("Validation Error: Variant ID must be greater than 0.");
            return false;
        }
        return variantRepository.deleteById(variant_id);
    }

    @Override
    public boolean archiveVariant(int variant_id) {
        if (variant_id <= 0) {
            System.out.println("Validation Error: Variant ID must be greater than 0.");
            return false;
        }
        return variantRepository.archiveById(variant_id);
    }

    @Override
    public boolean restoreVariant(int variant_id) {
        if (variant_id <= 0) {
            System.out.println("Validation Error: Variant ID must be greater than 0.");
            return false;
        }
        Product_Variant variant = variantRepository.findById(variant_id);
        if (variant == null) {
            System.out.println("Validation Error: Variant with ID " + variant_id + " was not found.");
            return false;
        }
        if (!variant.isIs_archived()) {
            System.out.println("Validation Error: Variant " + variant_id + " is not archived.");
            return false;
        }
        if (!isUsableParent(variant.getProduct_id())) return false;
        return variantRepository.restoreById(variant_id);
    }

    @Override
    public List<Product_Variant> searchVariants(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            System.out.println("Validation Error: Search keyword is required.");
            return Collections.emptyList();
        }
        return variantRepository.searchBySku(keyword.trim());
    }

    @Override
    public Product_Variant findById(int variant_id) {
        if (variant_id <= 0) {
            System.out.println("Validation Error: Variant ID must be greater than 0.");
            return null;
        }
        return variantRepository.findById(variant_id);
    }

    @Override
    public Product_Variant findBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            System.out.println("Validation Error: SKU is required.");
            return null;
        }
        return variantRepository.findBySku(sku.trim());
    }

    @Override
    public List<Product_Variant> getVariantsByProduct(int product_id) {
        if (product_id <= 0) {
            System.out.println("Validation Error: Product ID must be greater than 0.");
            return Collections.emptyList();
        }
        return variantRepository.findByProductId(product_id);
    }

    @Override
    public List<Product_Variant> getLowStockVariants() {
        return variantRepository.findLowStock();
    }

    @Override
    public boolean updateStock(int variant_id, int stock_quantity) {
        // Keep this old method for compatibility, but block unaudited stock changes.
        if (variant_id <= 0) {
            System.out.println("Validation Error: Variant ID must be greater than 0.");
            return false;
        }
        if (stock_quantity < 0) {
            System.out.println("Validation Error: Stock quantity cannot be negative.");
            return false;
        }
        System.out.println("Validation Error: Use Stock Movements to change inventory.");
        return false;
    }
}
