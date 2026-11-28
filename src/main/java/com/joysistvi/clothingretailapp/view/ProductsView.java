package com.joysistvi.clothingretailapp.view;

import com.joysistvi.clothingretailapp.model.Categories;
import com.joysistvi.clothingretailapp.model.Products;
import com.joysistvi.clothingretailapp.service.CategoriesService;
import com.joysistvi.clothingretailapp.service.ProductsService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ProductsView {
    private final ProductsService productsService;
    private final CategoriesService categoriesService;
    private final Scanner scanner;
    private final ConsoleInput input;

    public ProductsView(ProductsService productsService, CategoriesService categoriesService, Scanner scanner) {
        this.productsService = productsService;
        this.categoriesService = categoriesService;
        this.scanner = scanner;
        this.input = new ConsoleInput(scanner);
    }

    // Displays and handles the product menu.
    public void handleMenu() {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n=== PRODUCTS MANAGEMENT ===");
            System.out.println("1. View All Active Products");
            System.out.println("2. Create Product");
            System.out.println("3. Update Product");
            System.out.println("4. Search Products");
            System.out.println("5. View Active Products By Category");
            System.out.println("6. Archive Product");
            System.out.println("7. Restore Product");
            System.out.println("0. Back");
            try {
                int choice = input.integer("Choose an option: ");
                switch (choice) {
                    case 1 -> displayAllProducts();
                    case 2 -> promptCreateProduct();
                    case 3 -> promptUpdateProduct();
                    case 4 -> promptSearchProducts();
                    case 5 -> promptProductsByCategory();
                    case 6 -> promptArchiveProduct();
                    case 7 -> promptRestoreProduct();
                    case 0 -> { return; }
                    default -> System.out.println("Invalid option. Please try again.");
                }
                if (!ConsoleOutput.pause(scanner)) return;
            } catch (NoSuchElementException | IllegalStateException e) {
                return;
            } catch (NumberFormatException e) {
                System.out.println("Validation Error: Enter a valid whole number.");
                if (!ConsoleOutput.pause(scanner)) return;
            } catch (RuntimeException e) {
                System.out.println("Operation failed: " + e.getMessage());
                if (!ConsoleOutput.pause(scanner)) return;
            }
        }
    }

    public void displayAllProducts() {
        ConsoleOutput.operationHeader("VIEW ACTIVE PRODUCTS");
        printProductTable("\n----- Active Products -----", productsService.findAllActive());
    }

    public void promptCreateProduct() {
        ConsoleOutput.operationHeader("CREATE PRODUCT");
        if (!displayActiveCategories()) return;
        int categoryId = input.positive("Enter category ID: ");
        String name = input.text("Enter product name: ");
        String description = input.text("Enter description (optional): ");
        boolean created = productsService.save(new Products(0, name, description, categoryId, null, false));
        System.out.println(created ? "Product created successfully!" : "Failed to create product.");
    }

    public void promptUpdateProduct() {
        ConsoleOutput.operationHeader("UPDATE PRODUCT");
        printProductTable("\n----- Active Products -----", productsService.findAllActive());
        int id = input.positive("Enter product ID to update: ");
        Products current = productsService.findById(id);
        if (current == null) {
            System.out.println("Product not found.");
            return;
        }
        String newCategory = input.text("New category ID (Enter to keep " + current.getCategoryId() + "): ");
        int categoryId = newCategory.isBlank() ? current.getCategoryId() : Integer.parseInt(newCategory);
        String name = input.text("Enter new name (Enter to keep current): ");
        String description = input.text("Enter new description (Enter to keep current): ");
        if (name.isBlank()) name = current.getName();
        if (description.isBlank()) description = current.getDescription();
        boolean updated = productsService.update(
                new Products(id, name, description, categoryId, null, current.isArchived()));
        System.out.println(updated ? "Product updated successfully!" : "No product updated.");
    }

    public void promptSearchProducts() {
        ConsoleOutput.operationHeader("SEARCH PRODUCTS");
        String keyword = input.text("Enter product name keyword: ");
        printProductTable("\n----- Search Results -----", productsService.searchByName(keyword));
    }

    // Prompts the user to products by category.
    public void promptProductsByCategory() {
        ConsoleOutput.operationHeader("VIEW PRODUCTS BY CATEGORY");
        if (!displayActiveCategories()) return;
        int categoryId = input.positive("Enter category ID: ");
        printProductTable("\n----- Active Products By Category -----",
                productsService.findActiveByCategory(categoryId));
    }


    public void promptArchiveProduct() {
        ConsoleOutput.operationHeader("ARCHIVE PRODUCT");
        printProductTable("\n----- Active Products -----", productsService.findAllActive());
        int id = input.positive("Enter product ID to archive: ");
        boolean archived = productsService.archiveById(id);
        System.out.println(archived ? "Product archived successfully!" : "No product archived.");
    }

    public void promptRestoreProduct() {
        ConsoleOutput.operationHeader("RESTORE PRODUCT");
        printProductTable("\n----- Archived Products -----", productsService.findAllArchived());
        int id = input.positive("Enter product ID to restore: ");
        boolean restored = productsService.restoreById(id);
        System.out.println(restored ? "Product restored successfully!" : "No product restored.");
    }

    public void promptDeleteProduct() {
        ConsoleOutput.operationHeader("DELETE PRODUCT (MAINTENANCE)");
        int id = input.positive("Enter product ID to permanently delete: ");
        String confirm = input.text("Type YES to confirm permanent deletion: ");
        if (!"YES".equals(confirm)) {
            System.out.println("Delete cancelled.");
            return;
        }
        boolean deleted = productsService.deleteById(id);
        System.out.println(deleted ? "Product deleted." : "Product could not be deleted.");
    }

    // Displays available categories before asking for a category ID.
    private boolean displayActiveCategories() {
        List<Categories> categories = categoriesService.findAllActive();
        String border = "+------+----------------------+";
        System.out.println("\n----- Active Categories -----");
        System.out.println(border);
        System.out.printf("| %-4s | %-20s |%n", "ID", "Name");
        System.out.println(border);

        if (categories == null || categories.isEmpty()) {
            System.out.println("No active categories available.");
            System.out.println(border);
            return false;
        }

        for (Categories category : categories) {
            System.out.printf("| %-4d | %-20s |%n", category.getId(),
                    ConsoleOutput.truncate(category.getName(), 20));
        }
        System.out.println(border);
        return true;
    }

    private void printProductTable(String title, List<Products> products) {
        String border = "+------+----------------------+------------------+------------------------------+";
        System.out.println(title);
        System.out.println(border);
        System.out.printf("| %-4s | %-20s | %-16s | %-28s |%n", "ID", "Name", "Category", "Description");
        System.out.println(border);
        if (products == null || products.isEmpty()) System.out.println("No products found.");
        else for (Products product : products) {
            System.out.printf("| %-4d | %-20s | %-16s | %-28s |%n", product.getId(),
                    ConsoleOutput.truncate(product.getName(), 20),
                    ConsoleOutput.truncate(product.getCategoryName(), 16),
                    ConsoleOutput.truncate(product.getDescription(), 28));
        }
        System.out.println(border);
    }
}
