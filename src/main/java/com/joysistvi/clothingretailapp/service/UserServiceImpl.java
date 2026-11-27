
package com.joysistvi.clothingretailapp.service;

import com.joysistvi.clothingretailapp.model.User;
import com.joysistvi.clothingretailapp.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UserServiceImpl implements UserService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findById(int id) {
        if (id <= 0) {
            return null;
        }

        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllActiveUsers() {
        return userRepository.findAllActive();
    }

    @Override
    public List<User> getAllArchivedUsers() {
        return userRepository.findAllArchived();
    }

    @Override
    public boolean createUser(String username, String rawPassword,
                              String full_name) {
        return createUser(username, rawPassword, full_name, ROLE_USER);
    }

    @Override
    public boolean createUser(String username, String rawPassword,
                              String full_name, String role) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("\nValidation Error: Username is required.");
            return false;
        }

        if (full_name == null || full_name.trim().isEmpty()) {
            System.out.println("\nValidation Error: Full name is required.");
            return false;
        }

        if (rawPassword == null || rawPassword.length() < 6) {
            System.out.println("\nValidation Error: Password must be at least 6 characters.");
            return false;
        }

        if (role == null ||
                (!role.trim().equalsIgnoreCase(ROLE_ADMIN) &&
                        !role.trim().equalsIgnoreCase(ROLE_USER))) {
            System.out.println("\nValidation Error: Role must be ADMIN or USER.");
            return false;
        }

        if (userRepository.findByUsername(username.trim()) != null) {
            System.out.println("\nValidation Error: Username is already taken.");
            return false;
        }

        String hashedPassword = BCrypt.hashpw(
                rawPassword, BCrypt.gensalt()
        );

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(hashedPassword);
        user.setFull_name(full_name.trim());
        user.setRole(role.trim().toUpperCase(Locale.ROOT));

        return userRepository.save(user);
    }

    @Override
    public boolean updateUser(int id, String username, String full_name) {
        if (id <= 0) {
            System.out.println("\nValidation Error: User ID must be greater than 0.");
            return false;
        }

        if (username == null || username.trim().isEmpty()) {
            System.out.println("\nValidation Error: Username is required.");
            return false;
        }

        if (full_name == null || full_name.trim().isEmpty()) {
            System.out.println("\nValidation Error: Full name is required.");
            return false;
        }

        User user = userRepository.findById(id);
        if (user == null) {
            System.out.println("\nValidation Error: User not found.");
            return false;
        }

        User sameName = userRepository.findByUsername(username.trim());
        if (sameName != null && sameName.getId() != id) {
            System.out.println("\nValidation Error: Username is already taken.");
            return false;
        }

        user.setUsername(username.trim());
        user.setFull_name(full_name.trim());

        return userRepository.update(user);
    }

    @Override
    public boolean deleteUser(int id) {
        if (id <= 0) {
            System.out.println("\nValidation Error: User ID must be greater than 0.");
            return false;
        }

        return userRepository.deleteById(id);
    }

    @Override
    public boolean archiveUser(int id) {
        if (id <= 0) {
            System.out.println("\nValidation Error: User ID must be greater than 0.");
            return false;
        }

        return userRepository.archiveById(id);
    }

    @Override
    public boolean restoreUser(int id) {
        if (id <= 0) {
            System.out.println("\nValidation Error: User ID must be greater than 0.");
            return false;
        }

        return userRepository.restoreById(id);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            System.out.println("\nValidation Error: Search keyword is required.");
            return Collections.emptyList();
        }

        return userRepository.searchByUsername(keyword.trim());
    }

    @Override
    public boolean loginUser(String username, String candidatePassword) {
        return authenticateUser(username, candidatePassword) != null;
    }

    @Override
    public User authenticateUser(String username, String candidatePassword) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("\nValidation Error: Username is required to log in.");
            return null;
        }

        if (candidatePassword == null || candidatePassword.isEmpty()) {
            System.out.println("\nValidation Error: Password is required to log in.");
            return null;
        }

        User user = userRepository.findByUsername(username.trim());

        if (user == null || user.isArchived() ||
                user.getPasswordHash() == null) {
            return null;
        }

        try {
            if (BCrypt.checkpw(candidatePassword, user.getPasswordHash())) {
                return user;
            }
        } catch (IllegalArgumentException e) {
            // Invalid or old plaintext passwords cannot log in.
        }

        return null;
    }
}
