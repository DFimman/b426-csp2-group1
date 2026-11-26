package com.joysistvi.clothingretailapp.service;
import com.joysistvi.clothingretailapp.model.User;
import java.util.List;

public interface UserService {

    public List<User> getAllActiveUsers();
    public List<User> getAllArchivedUsers();
    public boolean createUser(String username, String rawPassword, String full_name, String role);
    public boolean createUser(String username, String rawPassword, String full_name);
    public boolean updateUser(int id, String username, String full_name);
    public boolean deleteUser(int id);
    public boolean archiveUser(int id);
    public boolean restoreUser(int id);
    public List<User> searchUsers(String keyword);
    public boolean loginUser(String username, String candidatePassword);
    public User authenticateUser(String username, String candidatePassword);
    User findById(int id);
}