package com.joysistvi.clothingretailapp.repository;

import com.joysistvi.clothingretailapp.config.DbConnection;
import com.joysistvi.clothingretailapp.model.Product_Variant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Product_VariantRepositoryImpl implements Product_VariantRepository {
    private final Connection connection;

    public Product_VariantRepositoryImpl(DbConnection dbConnection) {
        try {
            this.connection = dbConnection.connect();
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the database", e);
        }
    }

    private Product_Variant mapRow(ResultSet rs) throws SQLException {
        Product_Variant variant = new Product_Variant();
        variant.setVariant_id(rs.getInt("variant_id"));
        variant.setProduct_id(rs.getInt("product_id"));
        variant.setSku(rs.getString("sku"));
        variant.setSize(rs.getString("size"));
        variant.setColor(rs.getString("color"));
        variant.setUnit_price(rs.getDouble("unit_price"));
        variant.setStock_quantity(rs.getInt("stock_quantity"));
        variant.setReorder_level(rs.getInt("reorder_level"));
        variant.setIs_archived(rs.getBoolean("is_archived"));
        return variant;
    }

    private List<Product_Variant> queryList(String sql, Object param) {
        List<Product_Variant> variants = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) variants.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Repository Error (queryList): " + e.getMessage());
        }
        return variants;
    }

    private Product_Variant queryOne(String sql, Object param) {
        List<Product_Variant> results = queryList(sql, param);
        return results.isEmpty() ? null : results.get(0);
    }

    private boolean updateById(String sql, int variantId) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, variantId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Repository Error (updateById): " + e.getMessage());
            return false;
        }
    }

    @Override
    public Product_Variant findById(int variant_id) {
        return queryOne("SELECT * FROM product_variants WHERE variant_id = ?", variant_id);
    }

    @Override
    public Product_Variant findBySku(String sku) {
        return queryOne("SELECT * FROM product_variants WHERE sku = ?", sku);
    }

    @Override
    public List<Product_Variant> findByProductId(int product_id) {
        return queryList("SELECT * FROM product_variants WHERE product_id = ?", product_id);
    }

    @Override
    public List<Product_Variant> findAllActive() {
        // Archived parent products and categories must not appear in the catalog.
        return queryList("SELECT v.* FROM product_variants v "
                + "JOIN products p ON p.product_id = v.product_id "
                + "JOIN categories c ON c.category_id = p.category_id "
                + "WHERE v.is_archived = 0 AND p.is_archived = 0 AND c.is_archived = 0", null);
    }

    @Override
    public List<Product_Variant> findAllArchived() {
        return queryList("SELECT * FROM product_variants WHERE is_archived = 1", null);
    }

    @Override
    public List<Product_Variant> findLowStock() {
        return queryList("SELECT v.* FROM product_variants v "
                + "JOIN products p ON p.product_id = v.product_id "
                + "JOIN categories c ON c.category_id = p.category_id "
                + "WHERE v.stock_quantity <= v.reorder_level "
                + "AND v.is_archived = 0 AND p.is_archived = 0 AND c.is_archived = 0", null);
    }

    @Override
    public boolean save(Product_Variant variant) {
        String sql = "INSERT INTO product_variants "
                + "(product_id, sku, size, color, unit_price, stock_quantity, reorder_level) "
                + "VALUES (?, ?, ?, ?, ?, 0, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, variant.getProduct_id());
            ps.setString(2, variant.getSku());
            ps.setString(3, variant.getSize());
            ps.setString(4, variant.getColor());
            ps.setDouble(5, variant.getUnit_price());
            ps.setInt(6, variant.getReorder_level());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) variant.setVariant_id(keys.getInt(1));
                }
                variant.setStock_quantity(0);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Repository Error (save): " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean update(Product_Variant variant) {
        // Do not update stock_quantity here. StockMovementService records stock changes.
        String sql = "UPDATE product_variants SET product_id = ?, sku = ?, size = ?, color = ?, "
                + "unit_price = ?, reorder_level = ? WHERE variant_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, variant.getProduct_id());
            ps.setString(2, variant.getSku());
            ps.setString(3, variant.getSize());
            ps.setString(4, variant.getColor());
            ps.setDouble(5, variant.getUnit_price());
            ps.setInt(6, variant.getReorder_level());
            ps.setInt(7, variant.getVariant_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Repository Error (update): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStock(int variant_id, int stock_quantity) {
        // Kept for interface compatibility. All stock changes need a movement record.
        System.out.println("Use Stock Movements to adjust inventory.");
        return false;
    }

    // Permanent deletion stays in the repository but is not a normal store operation.
    @Override
    public boolean deleteById(int variant_id) {
        return updateById("DELETE FROM product_variants WHERE variant_id = ?", variant_id);
    }

    @Override
    public boolean archiveById(int variant_id) {
        return updateById("UPDATE product_variants SET is_archived = 1 WHERE variant_id = ?", variant_id);
    }

    @Override
    public boolean restoreById(int variant_id) {
        return updateById("UPDATE product_variants SET is_archived = 0 WHERE variant_id = ?", variant_id);
    }

    @Override
    public List<Product_Variant> searchBySku(String keyword) {
        return queryList("SELECT * FROM product_variants WHERE sku LIKE ?", "%" + keyword + "%");
    }
}
