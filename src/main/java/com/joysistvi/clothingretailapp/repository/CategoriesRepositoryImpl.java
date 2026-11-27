package com.joysistvi.clothingretailapp.repository;

import com.joysistvi.clothingretailapp.config.DbConnection;
import com.joysistvi.clothingretailapp.model.Categories;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriesRepositoryImpl implements CategoriesRepository {
    private final DbConnection dbConnection;

    // Initializes CategoriesRepositoryImpl with its required dependencies
    public CategoriesRepositoryImpl(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // Lists active categories.
    @Override
    public List<Categories> findAllActive() {
        return findByArchiveStatus(false);
    }

    // Lists archived categories.
    @Override
    public List<Categories> findAllArchived() {
        return findByArchiveStatus(true);
    }

    // Lists category records by archive status.
    private List<Categories> findByArchiveStatus(boolean archived) {

        List<Categories> categories = new ArrayList<>();
        String query = "SELECT category_id, name, description, is_archived FROM categories "
                + "WHERE is_archived = ? ORDER BY category_id ASC";

        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {

            prep.setBoolean(1, archived);

            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) categories.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("Repository Error (findByArchiveStatus): " + e.getMessage());
        }
        return categories;
    }

    // Saves a category record to the database.
    @Override
    public boolean save(Categories category) {
        String query = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {

            prep.setString(1, category.getName());
            prep.setString(2, category.getDescription());

            return prep.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Repository Error (save): " + e.getMessage());
            return false;
        }
    }

    // Updates a category record in the database.
    @Override
    public boolean update(Categories category) {
        String query = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {

            prep.setString(1, category.getName());
            prep.setString(2, category.getDescription());
            prep.setInt(3, category.getId());

            return prep.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Repository Error (update): " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        return updateFlag("DELETE FROM categories WHERE category_id = ?", id);
    }

    // Archives a category record by ID.
    @Override
    public boolean archiveById(int id) {
        return updateFlag("UPDATE categories SET is_archived = 1 WHERE category_id = ?", id);
    }

    // Restores an archived category record by ID.
    @Override
    public boolean restoreById(int id) {
        return updateFlag("UPDATE categories SET is_archived = 0 WHERE category_id = ?", id);
    }

    // Runs the requested database update for a record ID.
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

    // Searches categories by name.
    @Override
    public List<Categories> searchByName(String keyword) {

        List<Categories> categories = new ArrayList<>();
        String query = "SELECT category_id, name, description, is_archived FROM categories "
                + "WHERE is_archived = 0 AND name LIKE ? ORDER BY category_id ASC";

        try (Connection conn = dbConnection.connect();
             PreparedStatement prep = conn.prepareStatement(query)) {

            prep.setString(1, "%" + keyword + "%");

            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) categories.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.out.println("Repository Error (searchByName): " + e.getMessage());
        }
        return categories;
    }

    // Finds a category record by ID.
    @Override
    public Categories findById(int id) {
        String query = "SELECT category_id, name, description, is_archived FROM categories WHERE category_id = ?";
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

    // Converts one database row to a category object.
    private Categories mapRow(ResultSet rs) throws SQLException {
        return new Categories(rs.getInt("category_id"), rs.getString("name"),
                rs.getString("description"), rs.getBoolean("is_archived"));
    }
}
