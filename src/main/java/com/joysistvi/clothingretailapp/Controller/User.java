package com.joysistvi.clothingretailapp.Controller;
import java.sql.Timestamp;
public class User {
    private int id;
    private String username;
    private String password;
    private String full_name;
    private String role;
    private boolean archived;
    private Timestamp created_at;

    public User() {
    }

    public User(int id, String username, String password, String full_name, String role, Timestamp created_at) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.full_name = full_name;
        this.role = role;
        this.created_at = created_at;

    }

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public void setCreated_at(Timestamp createdAt) {
        this.created_at = createdAt;
    }

    public Timestamp isCreated_at() {
        return created_at;
    }


}
