package com.joysistvi.clothingretailapp;

import com.joysistvi.clothingretailapp.config.DbConnection;
import com.joysistvi.clothingretailapp.repository.*;
import com.joysistvi.clothingretailapp.service.*;
import com.joysistvi.clothingretailapp.view.*;

import java.util.Scanner;

public class App {
    private final DbConnection dbConnection = new DbConnection();
    private final Scanner scanner = new Scanner(System.in);

    // Categories and Products
    private final CategoriesRepository categoriesRepository = new CategoriesRepositoryImpl(dbConnection);
    private final ProductsRepository productsRepository = new ProductsRepositoryImpl(dbConnection);
    private final CategoriesService categoriesService = new CategoriesServiceImpl(categoriesRepository, productsRepository);
    private final ProductsService productsService = new ProductsServiceImpl(productsRepository, categoriesRepository);
    private final CategoriesView categoriesView = new CategoriesView(categoriesService, scanner);
    private final ProductsView productsView = new ProductsView(productsService, scanner);

    // Product Variant
    private final Product_VariantRepository productVariantRepository = new Product_VariantRepositoryImpl(dbConnection);
    private final Product_VariantService productVariantService = new Product_VariantServiceImpl(
            productVariantRepository, productsRepository, categoriesRepository);
    private final Product_VariantView productVariantView = new Product_VariantView(productVariantService, scanner);

    // User
    private final UserRepository userRepository = new UserRepositoryImpl(dbConnection);
    private final UserService userService = new UserServiceImpl(userRepository);
    private final UserView userView = new UserView(userService, scanner);

    // Sales and Stock Movements
    private final SaleRepository saleRepository = new SaleRepositoryImpl();
    private final SaleItemRepository saleItemRepository = new SaleItemRepositoryImpl();
    private final StockMovementRepository stockMovementRepository = new StockMovementRepositoryImpl();
    private final TransactionGuardRepository transactionGuardRepository = new TransactionGuardRepository();

    private final SaleService saleService = new SaleServiceImpl(dbConnection, saleRepository, saleItemRepository,
            stockMovementRepository, transactionGuardRepository);
    private final StockMovementService stockMovementService = new StockMovementServiceImpl(
            dbConnection, stockMovementRepository, transactionGuardRepository);

    private final SaleItemView saleItemView = new SaleItemView();
    private final SaleView saleView = new SaleView(saleService, saleItemView, scanner);
    private final StockMovementView stockMovementView = new StockMovementView(stockMovementService, scanner);

    // Starts the application with commented manual-testing examples.
    public static void main(String[] args) {
        App app = new App();

        // ==========================================
        // USER MANUAL TESTING
        // ==========================================

        // app.userView.handleMenu();

        // View Users
        // app.userView.displayAllUsers();
        // app.userView.displayAllUsers(true);

        // User CRUD
        // app.userView.promptCreateUser();
        // app.userView.promptUpdateUser();
        // app.userView.promptDeleteUser();

        // Archive and Restore
        // app.userView.promptArchiveUser();
        // app.userView.promptRestoreUser();

        // Search and Login
        // app.userView.promptSearchUsers();
        // app.userView.promptLoginUser();


        // ==========================================
        // CATEGORIES MANUAL TESTING
        // ==========================================

        // Category Management Menu
        // app.categoriesView.handleMenu();

        // View Categories
        // app.categoriesView.displayAllCategories();
        // System.out.println(app.categoriesService.findAllArchived().size());

        // Category CRUD
        // app.categoriesView.promptCreateCategory();
        // app.categoriesView.promptUpdateCategory();
        // System.out.println(app.categoriesService.findById(1));

        // Search Categories
        // app.categoriesView.promptSearchCategories();
        // System.out.println(app.categoriesService.searchByName("Shirts").size());

        // Archive and Restore
        // Archive every active product in the category before archiving the category.
        // app.categoriesView.promptArchiveCategory();
        // app.categoriesView.promptRestoreCategory();

        // Delete Category (maintenance only; not in the normal menu)
        // app.categoriesView.promptDeleteCategory();


        // ==========================================
        // PRODUCTS MANUAL TESTING
        // ==========================================

        // Product Management Menu
        // app.productsView.handleMenu();

        // View Products
        // app.productsView.displayAllProducts();
        // System.out.println(app.productsService.findAllArchived().size());
        // app.productsView.promptProductsByCategory();

        // Product CRUD
        // app.productsView.promptCreateProduct();
        // app.productsView.promptUpdateProduct();
        // System.out.println(app.productsService.findById(1));

        // Search Products
        // app.productsView.promptSearchProducts();
        // System.out.println(app.productsService.searchByName("Shirt").size());

        // Archive and Restore
        // Restore the parent category first if it is archived.
        // app.productsView.promptArchiveProduct();
        // app.productsView.promptRestoreProduct();

        // Delete Product (maintenance only; not in the normal menu)
        // app.productsView.promptDeleteProduct();


        // ==========================================
        // PRODUCT VARIANT MANUAL TESTING
        // ==========================================

        // Product Catalog Menu
        // app.productVariantView.handleMenu();

        // View Variants
        // System.out.println(app.productVariantService.getAllActiveVariants().size());
        // System.out.println(app.productVariantService.getAllArchivedVariants().size());
        // System.out.println(app.productVariantService.getLowStockVariants().size());

        // Find Variant
        // System.out.println(app.productVariantService.findById(1));
        // System.out.println(app.productVariantService.findBySku("TEST-001"));
        // System.out.println(app.productVariantService.getVariantsByProduct(1).size());

        // Create Variant
        // New variants must start with zero stock. Restock through Stock Movements.
        // app.productVariantService.createVariant(
        //         1, "TEST-001", "M", "Black", 499.00, 0, 5
        // );

        // Update Variant
        // app.productVariantService.updateVariant(
        //         1, 1, "TEST-001", "L", "Black", 599.00, 0, 5
        // );

        // Search Variant
        // System.out.println(app.productVariantService.searchVariants("TEST").size());

        // Archive and Restore
        // app.productVariantService.archiveVariant(1);
        // app.productVariantService.restoreVariant(1);

        // Delete Variant (maintenance only)
        // app.productVariantService.deleteVariant(1);


        // ==========================================
        // SALES MANUAL TESTING
        // ==========================================

        // Replace the ID with an active user_id for manual testing.
        // app.saleView.handleMenu(1);

        // Menu Operations:
        // 1. New Cash Sale
        // 2. Sales History
        // 3. Reprint Receipt


        // ==========================================
        // SALE ITEMS MANUAL TESTING
        // ==========================================

        // Sale Items are inserted during checkout and displayed on the receipt.
        // Use the Sales menu to create a sale, then reprint its receipt.
        // Sale Items do not have a separate CRUD menu.


        // ==========================================
        // STOCK MOVEMENT MANUAL TESTING
        // ==========================================

        // Replace the ID with an existing active ADMIN user_id.
        // Testing only; the authenticated session will supply this ID later.
        // app.stockMovementView.handleMenu(1);

        // Menu Operations:
        // 1. Restock Variant
        // 2. Adjust Stock (+/-)
        // 3. View Stock Movement History
    }
}
