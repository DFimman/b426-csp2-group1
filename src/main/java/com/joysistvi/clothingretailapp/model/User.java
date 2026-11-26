
package com.joysistvi.clothingretailapp.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String full_name;
    private String role;
    private boolean archived;
    private Timestamp created_at;

    public User() {
    }

    public User(int id, String username, String passwordHash,
                String full_name, String role, Timestamp created_at) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.full_name = full_name;
        this.role = role;
        this.created_at = created_at;
    }

    public User(String username, String passwordHash,
                String full_name, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.full_name = full_name;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }
}
