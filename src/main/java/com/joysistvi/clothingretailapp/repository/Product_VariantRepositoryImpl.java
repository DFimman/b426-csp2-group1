package com.joysistvi.clothingretailapp.repository;

import com.joysistvi.clothingretailapp.controller.Product_Variant;
import com.joysistvi.clothingretailapp.config.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        variant.setUnit_prize(rs.getDouble("unit_prize"));
        variant.setStock_quantity(rs.getInt("stock_quantity"));
        variant.setReorder_level(rs.getInt("reorder_level"));
        variant.setIs_archived(rs.getBoolean("is_archived"));
        return variant;
    }


    private List<Product_Variant> queryList(String sql, Object param) {
        List<Product_Variant> variants = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (param != null) {
                ps.setObject(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    variants.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return variants;
    }


    private Product_Variant queryOne(String sql, Object param) {
        List<Product_Variant> result = queryList(sql, param);
        return result.isEmpty() ? null : result.get(0);
    }

    // Runs an UPDATE/DELETE that only needs a variant_id
    private boolean updateById(String sql, int variant_id) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, variant_id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
        return queryList("SELECT * FROM product_variants WHERE is_archived = FALSE", null);
    }

    @Override
    public List<Product_Variant> findAllArchived() {
        return queryList("SELECT * FROM product_variants WHERE is_archived = TRUE", null);
    }

    @Override
    public List<Product_Variant> findLowStock() {
        return queryList("SELECT * FROM product_variants "
                + "WHERE stock_quantity <= reorder_level AND is_archived = FALSE", null);
    }

    @Override
    public boolean save(Product_Variant variant) {
        String sql = "INSERT INTO product_variants "
                + "(product_id, sku, size, color, unit_prize, stock_quantity, reorder_level) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, variant.getProduct_id());
            ps.setString(2, variant.getSku());
            ps.setString(3, variant.getSize());
            ps.setString(4, variant.getColor());
            ps.setDouble(5, variant.getUnit_prize());
            ps.setInt(6, variant.getStock_quantity());
            ps.setInt(7, variant.getReorder_level());
            if (ps.executeUpdate() > 0) {
                // Put the generated variant_id back on the object
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        variant.setVariant_id(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(Product_Variant variant) {
        String sql = "UPDATE product_variants SET product_id = ?, sku = ?, size = ?, color = ?, "
                + "unit_prize = ?, stock_quantity = ?, reorder_level = ? WHERE variant_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, variant.getProduct_id());
            ps.setString(2, variant.getSku());
            ps.setString(3, variant.getSize());
            ps.setString(4, variant.getColor());
            ps.setDouble(5, variant.getUnit_prize());
            ps.setInt(6, variant.getStock_quantity());
            ps.setInt(7, variant.getReorder_level());
            ps.setInt(8, variant.getVariant_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStock(int variant_id, int stock_quantity) {
        String sql = "UPDATE product_variants SET stock_quantity = ? WHERE variant_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, stock_quantity);
            ps.setInt(2, variant_id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(int variant_id) {
        return updateById("DELETE FROM product_variants WHERE variant_id = ?", variant_id);
    }

    @Override
    public boolean archiveById(int variant_id) {
        return updateById("UPDATE product_variants SET is_archived = TRUE WHERE variant_id = ?", variant_id);
    }

    @Override
    public boolean restoreById(int variant_id) {
        return updateById("UPDATE product_variants SET is_archived = FALSE WHERE variant_id = ?", variant_id);
    }

    @Override
    public List<Product_Variant> searchBySku(String keyword) {
        return queryList("SELECT * FROM product_variants WHERE sku LIKE ?", "%" + keyword + "%");
    }
}