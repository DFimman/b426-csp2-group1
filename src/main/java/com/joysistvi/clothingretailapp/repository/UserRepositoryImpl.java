
package com.joysistvi.clothingretailapp.repository;

import com.joysistvi.clothingretailapp.config.DbConnection;
import com.joysistvi.clothingretailapp.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {

    private final Connection connection;

    public UserRepositoryImpl(DbConnection dbConnection) {
        try {
            this.connection = dbConnection.connect();
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the database", e);
        }
    }

    // Converts one database row to a user object.
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFull_name(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setArchived(rs.getBoolean("is_archived"));
        user.setCreated_at(rs.getTimestamp("created_at"));
        return user;
    }

    // Executes a query and returns matching records.
    private List<User> queryList(String sql, String param) {
        List<User> users = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    private boolean updateById(String sql, int id) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<User> findAllActive() {
        return queryList(
                "SELECT * FROM users WHERE is_archived = FALSE",
                null
        );
    }

    @Override
    public List<User> findAllArchived() {
        return queryList(
                "SELECT * FROM users WHERE is_archived = TRUE",
                null
        );
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean save(User user) {
        String sql = "INSERT INTO users " +
                "(username, password_hash, full_name, role) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFull_name());
            ps.setString(4, user.getRole());

            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
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
    public boolean update(User user) {
        String sql = "UPDATE users " +
                "SET username = ?, full_name = ? " +
                "WHERE user_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFull_name());
            ps.setInt(3, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        return updateById(
                "DELETE FROM users WHERE user_id = ?", id
        );
    }


    @Override
    public boolean archiveById(int id) {
        return updateById(
                "UPDATE users SET is_archived = TRUE WHERE user_id = ?",
                id
        );
    }

    @Override
    public boolean restoreById(int id) {
        return updateById(
                "UPDATE users SET is_archived = FALSE WHERE user_id = ?",
                id
        );
    }

    @Override
    public List<User> searchByUsername(String keyword) {
        return queryList(
                "SELECT * FROM users WHERE username LIKE ?",
                "%" + keyword + "%"
        );
    }
}
