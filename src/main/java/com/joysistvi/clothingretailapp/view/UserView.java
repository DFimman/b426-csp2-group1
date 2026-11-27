package com.joysistvi.clothingretailapp.view;

import com.joysistvi.clothingretailapp.model.User;
import com.joysistvi.clothingretailapp.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import static com.joysistvi.clothingretailapp.view.ConsoleOutput.clearScreen;
import static com.joysistvi.clothingretailapp.view.ConsoleOutput.truncate;

public class UserView {

    private final UserService userService;
    private final Scanner scanner;

    public UserView(UserService userService, Scanner scanner) {
        this.userService = userService;
        this.scanner = scanner;
    }

    // Displays and handles the user menu.
    public void handleMenu() {
        while (true) {
            clearScreen();
            System.out.println("\n=== MANAGE USERS ===");
            System.out.println("1. view All Users");
            System.out.println("2. Create/Register User");
            System.out.println("3. Update User");
            System.out.println("4. Search User");
            System.out.println("5. Archive User");
            System.out.println("6. Restore User");
            System.out.println("7. Delete User");
            System.out.println("0. Back");

            Integer choice = promptForInt("Choose an option: ");
            if (choice == null) {
                return;
            }

            switch (choice) {
                case 1 -> displayAllUsers();
                case 2 -> promptCreateUser();
                case 3 -> promptUpdateUser();
                case 4 -> promptSearchUsers();
                case 5 -> promptArchiveUser();
                case 6 -> promptRestoreUser();
                case 7 -> promptDeleteUser();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
            if (!ConsoleOutput.pause(scanner)) return;
        }
    }


    public void displayAllUsers() {
        displayAllUsers(false);
    }

    public void displayAllUsers(boolean isArchived) {
        clearScreen();
        List<User> users = isArchived ? userService.getAllArchivedUsers() : userService.getAllActiveUsers();

        String title = isArchived ? "Archived Users" : "Users";
        printUsers(users, title);
    }


    public void promptCreateUser() {
        clearScreen();
        System.out.println("\n----- Create New User -----");

        String username = getStringInput("Enter username: ");
        if (username == null) {
            return;
        }

        String password = getStringInput("Enter password: ");
        if (password == null) {
            return;
        }

        String fullName = getStringInput("Enter full name: ");
        if (fullName == null) {
            return;
        }

        String role = "USER";

        boolean created = userService.createUser(
                username, password, fullName, role
        );

        if (created) {
            System.out.println(
                    "\nUser '" + username.trim() +
                            "' created successfully!"
            );
        } else {
            System.out.println("\nFailed to create user.");
        }
    }


    public void promptUpdateUser() {
        clearScreen();
        System.out.println("\n----- Update User -----");
        Integer id = getIntInput("Enter user id: ");
        if (id == null) return;

        User existing = userService.findById(id);
        if (existing == null) {
            System.out.println("\nNo user found with ID " + id + ".");
            return;
        }
        System.out.println("Current: " + existing.getUsername() + " | " + existing.getFull_name());
        System.out.println("1. Username");
        System.out.println("2. Full Name");
        System.out.println("0. Cancel");
        Integer choice = promptForInt("Which detail to change? ");
        if (choice == null || choice == 0) return;
        String username = existing.getUsername();
        String fullName = existing.getFull_name();
        switch (choice) {
            case 1 -> { username = getStringInput("Enter new username: "); if (username == null) return; }
            case 2 -> { fullName = getStringInput("Enter new full name: "); if (fullName == null) return; }
            default -> { System.out.println("Invalid option."); return; }
        }
        boolean updated = userService.updateUser(id, username, fullName);
        System.out.println(updated ? "\nUser updated successfully!" : "\nFailed to update user.");
    }

    public void promptDeleteUser() {
        clearScreen();
        System.out.println("\n----- Delete User -----");
        Integer id = getIntInput("Enter user id: ");
        if (id == null) {
            return;
        }

        boolean deleted = userService.deleteUser(id);

        if (deleted) {
            System.out.println("\nUser ID " + id + " deleted successfully!");
        } else {
            System.out.println("\nNo user found with ID " + id + ".");
        }
    }


    public void promptArchiveUser() {
        clearScreen();
        System.out.println("\n----- Archive User -----");
        Integer id = getIntInput("Enter user id: ");
        if (id == null) {
            return;
        }

        boolean archived = userService.archiveUser(id);

        if (archived) {
            System.out.println("\nUser ID " + id + " archived successfully!");
        } else {
            System.out.println("\nNo user found with ID " + id + ".");
        }
    }


    public void promptRestoreUser() {
        clearScreen();
        System.out.println("\n----- Restore User -----");
        List<User> users = userService.getAllArchivedUsers();
        printUsers(users, "Archived Users");

        if (users == null || users.isEmpty()) {
            return;
        }

        Integer id = getIntInput("Enter user id: ");
        if (id == null) {
            return;
        }

        boolean restored = userService.restoreUser(id);

        if (restored) {
            System.out.println("\nUser ID " + id + " restored successfully!");
        } else {
            System.out.println("\nNo user found with ID " + id + ".");
        }
    }

    public void promptSearchUsers() {
        clearScreen();
        System.out.println("\n----- Search Users -----");
        String keyword = getStringInput("Enter keyword (search): ");
        if (keyword == null) {
            return;
        }

        List<User> users = userService.searchUsers(keyword);
        printUsers(users, "Search Users");
    }


    public void promptLoginUser() {
        clearScreen();
        System.out.println("\n----- Login User -----");
        String username = getStringInput("Enter username: ");
        if (username == null) {
            return;
        }
        String password = getStringInput("Enter password: ");
        if (password == null) {
            return;
        }

        boolean loggedIn = userService.loginUser(username, password);

        if (loggedIn) {
            System.out.println("\nLogin successful! Welcome, " + username.trim() + "!");
        } else {
            System.out.println("\nLogin failed: Invalid username or password.");
        }
    }


    private Integer promptForInt(String prompt) {
        while (true) {
            String input = getStringInput(prompt);
            if (input == null) {
                return null;
            }

            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("\nValidation Error: Please enter a valid whole number.");
            }
        }
    }

    private String getStringInput(String prompt) {
        while (true) {
            System.out.print(prompt);

            try {
                String input = scanner.nextLine();

                if (input.trim().isEmpty()) {
                    System.out.println("\nValidation Error: Input cannot be blank.");
                    continue;
                }

                return input;
            } catch (NoSuchElementException | IllegalStateException e) {
                System.out.println("\nInput Error: Unable to read input. Operation cancelled.");
                return null;
            }
        }
    }

    private Integer getIntInput(String prompt) {
        while (true) {
            String input = getStringInput(prompt);
            if (input == null) {
                return null;
            }

            try {
                int number = Integer.parseInt(input.trim());
                if (number <= 0) {
                    System.out.println("\nValidation Error: Please enter a positive number.");
                    continue;
                }

                return number;
            } catch (NumberFormatException e) {
                System.out.println("\nValidation Error: Please enter a valid whole number.");
            }
        }
    }

    private void printUsers(List<User> users, String title) {
        System.out.println("\n----- " + title + " -----");
        System.out.printf("%-4s| %-20s| %-28s| %-12s%n", "ID", "Username", "Full Name", "Role");

        if (users == null || users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        for (User user : users) {
            System.out.printf("%-4d| %-20s| %-28s| %-12s%n",
                    user.getId(),
                    truncate(user.getUsername(), 20),
                    truncate(user.getFull_name(), 28),
                    truncate(user.getRole(), 12));
        }
    }
}
