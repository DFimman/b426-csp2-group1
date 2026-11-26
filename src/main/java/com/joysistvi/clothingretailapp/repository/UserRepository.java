package com.joysistvi.clothingretailapp.repository;
import com.joysistvi.clothingretailapp.controller.User;
import java.util.List;
public interface UserRepository {

    public User findById(int id);
    public List<User> findAllActive();
    public List<User> findAllArchived();
    public User findByUsername(String username);
    public boolean save(User user);
    public boolean update(User user);
    public boolean deleteById(int id);
    public boolean archiveById(int id);
    public boolean restoreById(int id);
    public List<User> searchByUsername(String keyword);
}
