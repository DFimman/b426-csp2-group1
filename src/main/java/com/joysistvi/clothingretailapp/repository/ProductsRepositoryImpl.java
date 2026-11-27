package com.joysistvi.clothingretailapp.repository;

import com.joysistvi.clothingretailapp.config.DbConnection;
import com.joysistvi.clothingretailapp.model.Products;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductsRepositoryImpl implements ProductsRepository {
    private static final String SELECT_PRODUCTS =
            "SELECT p.product_id, p.name, p.description, p.category_id, "
                    + "c.name AS category_name, p.is_archived FROM products p "
                    + "JOIN categories c ON p.category_id = c.category_id ";

    private final DbConnection dbConnection;

    public ProductsRepositoryImpl(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<Products> findAllActive() {
        return findByArchiveStatus(false);
    }

    @Override
    public List<Products> findAllArchived() {
        return findByArchiveStatus(true);
    }

    private List<Products> findByArchiveStatus(boolean archived) {
        List<Products> products = new ArrayList<>();
        String query = SELECT_PRODUCTS + "WHERE p.is_archived = ? ORDER BY p.product_id ASC";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {
            prep.setBoolean(1, archived);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Repository Error (findByArchiveStatus): " + e.getMessage());
        }
        return products;
    }

    @Override
    public List<Products> findActiveByCategory(int categoryId) {
        List<Products> products = new ArrayList<>();
        String query = SELECT_PRODUCTS
                + "WHERE p.is_archived = 0 AND p.category_id = ? ORDER BY p.product_id ASC";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {
            prep.setInt(1, categoryId);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Repository Error (findActiveByCategory): " + e.getMessage());
        }
        return products;
    }

    @Override
    public int countActiveByCategory(int categoryId) {
        String query = "SELECT COUNT(*) AS total FROM products WHERE is_archived = 0 AND category_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {
            prep.setInt(1, categoryId);
            try (ResultSet rs = prep.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
                throw new SQLException("Count query returned no row");
            }
        } catch (SQLException e) {
            // Do not allow category archiving if we cannot check its active products.
            throw new IllegalStateException("Cannot verify active products for category " + categoryId, e);
        }
    }

    @Override
    public boolean save(Products product) {
        String query = "INSERT INTO products (name, description, category_id) VALUES (?, ?, ?)";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {
            prep.setString(1, product.getName());
            prep.setString(2, product.getDescription());
            prep.setInt(3, product.getCategoryId());
            return prep.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Repository Error (save): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Products product) {
        String query = "UPDATE products SET name = ?, description = ?, category_id = ? WHERE product_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {
            prep.setString(1, product.getName());
            prep.setString(2, product.getDescription());
            prep.setInt(3, product.getCategoryId());
            prep.setInt(4, product.getId());
            return prep.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Repository Error (update): " + e.getMessage());
            return false;
        }
    }

    // Permanent deletion is kept for maintenance, not regular store operations.
    @Override
    public boolean deleteById(int id) {
        return updateFlag("DELETE FROM products WHERE product_id = ?", id);
    }

    @Override
    public boolean archiveById(int id) {
        return updateFlag("UPDATE products SET is_archived = 1 WHERE product_id = ?", id);
    }

    @Override
    public boolean restoreById(int id) {
        return updateFlag("UPDATE products SET is_archived = 0 WHERE product_id = ?", id);
    }

    private boolean updateFlag(String query, int id) {
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {
            prep.setInt(1, id);
            return prep.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Repository Error (updateFlag): " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Products> searchByName(String keyword) {
        List<Products> products = new ArrayList<>();
        String query = SELECT_PRODUCTS
                + "WHERE p.is_archived = 0 AND p.name LIKE ? ORDER BY p.product_id ASC";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {
            prep.setString(1, "%" + keyword + "%");
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Repository Error (searchByName): " + e.getMessage());
        }
        return products;
    }

    @Override
    public Products findById(int id) {
        String query = SELECT_PRODUCTS + "WHERE p.product_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {
            prep.setInt(1, id);
            try (ResultSet rs = prep.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("Repository Error (findById): " + e.getMessage());
            return null;
        }
    }

    private Products mapRow(ResultSet rs) throws SQLException {
        return new Products(rs.getInt("product_id"), rs.getString("name"),
                rs.getString("description"), rs.getInt("category_id"),
                rs.getString("category_name"), rs.getBoolean("is_archived"));
    }
}
