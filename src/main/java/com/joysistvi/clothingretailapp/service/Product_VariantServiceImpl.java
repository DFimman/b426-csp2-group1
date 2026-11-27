package com.joysistvi.clothingretailapp.service;

import com.joysistvi.clothingretailapp.model.Product_Variant;
import com.joysistvi.clothingretailapp.repository.Product_VariantRepository;

import java.util.Collections;
import java.util.List;

public class Product_VariantServiceImpl implements Product_VariantService {

    private final Product_VariantRepository variantRepository;

    public Product_VariantServiceImpl(Product_VariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    private String validateFields(int product_id, String sku, String size, String color, double unit_prize, int stock_quantity, int reorder_level) {
        if (product_id <= 0) {
            return "Product ID must be greater than 0.";
        }
        if (sku == null || sku.trim().isEmpty()) {
            return "SKU is required.";
        }
        if (size == null || size.trim().isEmpty()) {
            return "Size is required.";
        }
        if (color == null || color.trim().isEmpty()) {
            return "Color is required.";
        }
        if (unit_prize <= 0) {
            return "Unit price must be greater than 0.";
        }
        if (stock_quantity < 0) {
            return "Stock quantity cannot be negative.";
        }
        if (reorder_level < 0) {
            return "Reorder level cannot be negative.";
        }
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
    public boolean createVariant(int product_id, String sku, String size, String color, double unit_prize, int stock_quantity, int reorder_level) {
        String error = validateFields(product_id, sku, size, color, unit_prize, stock_quantity, reorder_level);
        if (error != null) {
            System.out.println("\nValidation Error: " + error);
            return false;
        }
        // SKU must be unique
        if (variantRepository.findBySku(sku.trim()) != null) {
            System.out.println("\nValidation Error: SKU is already used by another variant.");
            return false;
        }

        Product_Variant variant = new Product_Variant(product_id, sku.trim(), size.trim(), color.trim(),
                unit_prize, stock_quantity, reorder_level);
        return variantRepository.save(variant);
    }

    @Override
    public boolean updateVariant(int variant_id, int product_id, String sku, String size, String color, double unit_prize, int stock_quantity, int reorder_level) {
        if (variant_id <= 0) {
            System.out.println("\nValidation Error: Variant ID must be greater than 0.");
            return false;
        }
        String error = validateFields(product_id, sku, size, color, unit_prize, stock_quantity, reorder_level);
        if (error != null) {
            System.out.println("\nValidation Error: " + error);
            return false;
        }

        Product_Variant variant = variantRepository.findById(variant_id);
        if (variant == null) {
            System.out.println("\nValidation Error: Variant not found.");
            return false;
        }

        Product_Variant sameSku = variantRepository.findBySku(sku.trim());
        if (sameSku != null && sameSku.getVariant_id() != variant_id) {
            System.out.println("\nValidation Error: SKU is already used by another variant.");
            return false;
        }

        variant.setProduct_id(product_id);
        variant.setSku(sku.trim());
        variant.setSize(size.trim());
        variant.setColor(color.trim());
        variant.setUnit_price(unit_prize);
        variant.setStock_quantity(stock_quantity);
        variant.setReorder_level(reorder_level);
        return variantRepository.update(variant);
    }

    @Override
    public boolean deleteVariant(int variant_id) {
        if (variant_id <= 0) {
            System.out.println("\nValidation Error: Variant ID must be greater than 0.");
            return false;
        }

        return variantRepository.deleteById(variant_id);
    }

    @Override
    public boolean archiveVariant(int variant_id) {
        if (variant_id <= 0) {
            System.out.println("\nValidation Error: Variant ID must be greater than 0.");
            return false;
        }

        return variantRepository.archiveById(variant_id);
    }

    @Override
    public boolean restoreVariant(int variant_id) {
        if (variant_id <= 0) {
            System.out.println("\nValidation Error: Variant ID must be greater than 0.");
            return false;
        }

        return variantRepository.restoreById(variant_id);
    }

    @Override
    public List<Product_Variant> searchVariants(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("\nValidation Error: Search keyword is required.");
            return Collections.emptyList();
        }

        return variantRepository.searchBySku(keyword.trim());
    }

    @Override
    public Product_Variant findById(int variant_id) {
        if (variant_id <= 0) {
            return null;
        }

        return variantRepository.findById(variant_id);
    }

    @Override
    public Product_Variant findBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return null;
        }

        return variantRepository.findBySku(sku.trim());
    }

    @Override
    public List<Product_Variant> getVariantsByProduct(int product_id) {
        if (product_id <= 0) {
            System.out.println("\nValidation Error: Product ID must be greater than 0.");
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
        if (variant_id <= 0) {
            System.out.println("\nValidation Error: Variant ID must be greater than 0.");
            return false;
        }
        if (stock_quantity < 0) {
            System.out.println("\nValidation Error: Stock quantity cannot be negative.");
            return false;
        }

        return variantRepository.updateStock(variant_id, stock_quantity);
    }
}
