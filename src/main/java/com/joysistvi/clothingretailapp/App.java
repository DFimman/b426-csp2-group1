package com.joysistvi.clothingretailapp;

import com.joysistvi.clothingretailapp.config.DbConnection;
import com.joysistvi.clothingretailapp.model.User;
import com.joysistvi.clothingretailapp.repository.*;
import com.joysistvi.clothingretailapp.service.*;
import com.joysistvi.clothingretailapp.view.*;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class App {
    private final Scanner scanner;
    private final ConsoleInput input;
    private final UserService userService;
    private final UserView userView;
    private final CategoriesView categoriesView;
    private final ProductsView productsView;
    private final Product_VariantView productVariantView;
    private final ProductVariantAdminView variantAdminView;
    private final SaleView saleView;
    private final StockMovementView stockMovementView;
    private final SalesReportsView reportsView;

    // Connects the existing entity layers to the Session 3 menus.
    public App() {
        scanner = new Scanner(System.in);
        input = new ConsoleInput(scanner);
        DbConnection dbConnection = new DbConnection();

        CategoriesRepository categoriesRepository = new CategoriesRepositoryImpl(dbConnection);
        ProductsRepository productsRepository = new ProductsRepositoryImpl(dbConnection);
        CategoriesService categoriesService = new CategoriesServiceImpl(categoriesRepository, productsRepository);
        ProductsService productsService = new ProductsServiceImpl(productsRepository, categoriesRepository);
        categoriesView = new CategoriesView(categoriesService, scanner);
        productsView = new ProductsView(productsService, scanner);

        Product_VariantRepository variantRepository = new Product_VariantRepositoryImpl(dbConnection);
        Product_VariantService variantService = new Product_VariantServiceImpl(
                variantRepository, productsRepository, categoriesRepository);
        productVariantView = new Product_VariantView(variantService, productsService, scanner);
        variantAdminView = new ProductVariantAdminView(variantService, scanner);

        UserRepository userRepository = new UserRepositoryImpl(dbConnection);
        userService = new UserServiceImpl(userRepository);
        userView = new UserView(userService, scanner);

        SaleRepository saleRepository = new SaleRepositoryImpl();
        SaleItemRepository saleItemRepository = new SaleItemRepositoryImpl();
        StockMovementRepository stockMovementRepository = new StockMovementRepositoryImpl();
        TransactionGuardRepository guardRepository = new TransactionGuardRepository();
        SaleService saleService = new SaleServiceImpl(dbConnection, saleRepository,
                saleItemRepository, stockMovementRepository, guardRepository);
        StockMovementService stockMovementService = new StockMovementServiceImpl(
                dbConnection, stockMovementRepository, guardRepository);
        saleView = new SaleView(saleService, new SaleItemView(), scanner);
        stockMovementView = new StockMovementView(stockMovementService, scanner);
        reportsView = new SalesReportsView(saleService, variantService, productsService, scanner);
    }

    // Starts the program and handles database initialization errors.
    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    // Keeps the application running until Exit is selected.
    public void run() {
        boolean running = true;
        while (running) {
            try {
                ConsoleOutput.clearScreen();
                System.out.println("\n===== CIMS-POS =====");
                System.out.println("1. Login");
                System.out.println("0. Exit");

                int choice = readChoice();
                switch (choice) {
                    case 1 -> {
                        User user = login();
                        if (user != null) dashboard(user);
                    }
                    case 0 -> running = false;
                    default -> {
                        System.out.println("Invalid option. Please try again.");
                        if (!ConsoleOutput.pause(scanner)) running = false;
                    }
                }
            } catch (RuntimeException e) {
                System.out.println("Operation failed: " + e.getMessage());
                if (!ConsoleOutput.pause(scanner)) running = false;
            }
        }
        System.out.println("Thank you for using CIMS-POS. Goodbye!");
    }

    // Checks user credentials and allows only active ADMIN or USER accounts.
    private User login() {
        ConsoleOutput.clearScreen();
        System.out.println("\n===== LOGIN =====");
        String username = input.text("Username: ");
        String password = input.text("Password: ");
        User user = userService.authenticateUser(username, password);
        if (user == null || user.isArchived() || !validRole(user.getRole())) {
            System.out.println("Login failed: Invalid credentials or inactive account.");
            ConsoleOutput.pause(scanner);
            return null;
        }
        System.out.println("Login successful. Welcome, " + user.getFull_name() + "!");
        return user;
    }

    // Refreshes the logged-in user and displays the dashboard for their role.
    private void dashboard(User loggedIn) {
        // Do not carry an unfinished cart between different logged-in users.
        saleView.clearPendingCart();
        try {
            while (true) {
                User current = userService.findById(loggedIn.getId());
                if (current == null || current.isArchived() || !validRole(current.getRole())) {
                    System.out.println("Session ended: This account is no longer active.");
                    ConsoleOutput.pause(scanner);
                    return;
                }
                boolean admin = "ADMIN".equals(current.getRole());

                ConsoleOutput.clearScreen();
                System.out.println(admin ? "\n===== ADMIN DASHBOARD =====" : "\n===== CASHIER DASHBOARD =====");
                System.out.println("Signed in as: " + current.getFull_name() + " (" + current.getRole() + ")");
                if (admin) {
                    printAdminOptions();
                } else {
                    printCashierOptions();
                }

                int choice = readChoice();
                if (choice == 0) {
                    System.out.println("Logged out successfully.");
                    return;
                }

                if (admin) {
                    handleAdminChoice(choice, current.getId());
                } else {
                    handleCashierChoice(choice, current.getId());
                }
            }
        } finally {
            saleView.clearPendingCart();
        }
    }

    // Prints the main Admin operations without cluttering the dashboard.
    private void printAdminOptions() {
        System.out.println("1. Manage Cashier Accounts");
        System.out.println("2. Manage Product");
        System.out.println("3. Browse Catalog");
        System.out.println("4. Process Sales");
        System.out.println("5. Inventory Management");
        System.out.println("6. Sales Reports and Stock Analytics");
        System.out.println("0. Logout");
    }

    // Displays separate cart, checkout, history and receipt options for Cashiers.
    private void printCashierOptions() {
        System.out.println("1. Browse Catalog");
        System.out.println("2. Manage Cart");
        System.out.println("3. Cash Checkout");
        System.out.println("4. View My Sales");
        System.out.println("5. Reprint Receipt");
        System.out.println("0. Logout");
    }

    // Routes Admins to their management and sales menus.
    private void handleAdminChoice(int choice, int adminId) {
        switch (choice) {
            case 1 -> userView.handleMenu();
            case 2 -> manageProductMenu();
            case 3 -> productVariantView.handleMenu();
            case 4 -> saleView.handleMenu(adminId);
            case 5 -> inventoryManagementMenu(adminId);
            case 6 -> reportsView.handleMenu(adminId);
            default -> {
                System.out.println("Invalid option. Please try again.");
                ConsoleOutput.pause(scanner);
            }
        }
    }

    // Groups Categories, Products and Product Variants into one Admin submenu.
    private void manageProductMenu() {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n===== MANAGE PRODUCT =====");
            System.out.println("1. Manage Categories");
            System.out.println("2. Manage Products");
            System.out.println("3. Manage Product Variants");
            System.out.println("0. Back");

            int choice = readChoice();
            switch (choice) {
                case 1 -> categoriesView.handleMenu();
                case 2 -> productsView.handleMenu();
                case 3 -> variantAdminView.handleMenu();
                case 0 -> { return; }
                default -> System.out.println("Invalid option. Please try again.");
            }
            if (!ConsoleOutput.pause(scanner)) return;
        }
    }

    // Groups restocking, stock adjustments, history and low-stock alerts.
    private void inventoryManagementMenu(int adminId) {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n===== INVENTORY MANAGEMENT =====");
            System.out.println("1. Restock / Adjust / Stock Movement History");
            System.out.println("2. View Low-Stock Alerts");
            System.out.println("0. Back");

            int choice = readChoice();
            switch (choice) {
                case 1 -> stockMovementView.handleMenu(adminId);
                case 2 -> variantAdminView.displayLowStock();
                case 0 -> { return; }
                default -> System.out.println("Invalid option. Please try again.");
            }
            if (!ConsoleOutput.pause(scanner)) return;
        }
    }

    // Routes Cashiers to individual sale operations using their session user ID.
    private void handleCashierChoice(int choice, int userId) {
        switch (choice) {
            case 1 -> productVariantView.handleMenu();
            case 2 -> saleView.manageCart();
            case 3 -> saleView.checkoutCurrentCart(userId);
            case 4 -> saleView.showSalesHistory(userId);
            case 5 -> saleView.reprintSavedReceipt(userId);
            default -> {
                System.out.println("Invalid option. Please try again.");
                ConsoleOutput.pause(scanner);
                return;
            }
        }
        if (choice >= 2 && choice <= 5) ConsoleOutput.pause(scanner);
    }

    // Handles nonnumeric choices without terminating the program.
    private int readChoice() {
        try {
            return input.integer("Choose an option: ");
        } catch (NumberFormatException e) {
            System.out.println("Validation Error: Please enter a whole number.");
            return -1;
        }
    }

    // Checks that an account role is supported by the application.
    private boolean validRole(String role) {
        return "ADMIN".equals(role) || "USER".equals(role);
    }
}
