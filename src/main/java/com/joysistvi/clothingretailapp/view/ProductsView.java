package com.joysistvi.clothingretailapp.view;

import com.joysistvi.clothingretailapp.model.Products;
import com.joysistvi.clothingretailapp.service.ProductsService;

import java.util.List;
import java.util.Scanner;

public class ProductsView {
    private final ProductsService productsService;
    private final Scanner scanner;

    public ProductsView(ProductsService productsService, Scanner scanner) {
        this.productsService = productsService;
        this.scanner = scanner;
    }

    public void handleMenu() {
        while (true) {
            System.out.println("\n=== PRODUCTS MANAGEMENT ===");
            System.out.println("1. View All Active Products");
            System.out.println("2. Create Product");
            System.out.println("3. Update Product");
            System.out.println("4. Search Products");
            System.out.println("5. View Active Products By Category");
            System.out.println("6. Archive Product");
            System.out.println("7. Restore Product");
            System.out.println("8. Delete Product");
            System.out.println("0. Back");

            int choice = promptForInt("Choose an option: ");
            switch (choice) {
                case 1:
                    displayAllProducts();
                    break;
                case 2:
                    promptCreateProduct();
                    break;
                case 3:
                    promptUpdateProduct();
                    break;
                case 4:
                    promptSearchProducts();
                    break;
                case 5:
                    promptProductsByCategory();
                    break;
                case 6:
                    promptArchiveProduct();
                    break;
                case 7:
                    promptRestoreProduct();
                    break;
                case 8:
                    promptDeleteProduct();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    public void displayAllProducts() {
        printProductTable("\n----- Active Products -----",
                productsService.findAllActive(), "No active products found.");
    }

    public void promptCreateProduct() {
        System.out.println("\n=== CREATE Product ===");
        int categoryId = promptForInt("Enter category ID: ");
        System.out.print("Enter product name: ");
        String name = scanner.nextLine();
        System.out.print("Enter description (optional): ");
        String description = scanner.nextLine();

        boolean created = productsService.save(new Products(0, name, description, categoryId, null, false));
        if (created) {
            System.out.println("Product '" + name.trim() + "' created successfully!");
        } else {
            System.out.println("Failed to create product.");
        }
    }

    public void promptUpdateProduct() {
        System.out.println("\n=== UPDATE Product ===");
        displayAllProducts();
        int id = promptForInt("Enter product ID to update: ");

        Products current = productsService.findById(id);
        if (current == null) {
            System.out.println("No product updated (check ID or input).");
            return;
        }

        int categoryId = promptIntWithDefault("Enter new category ID", current.getCategoryId());
        System.out.print("Enter new name (Enter to keep '" + current.getName() + "'): ");
        String name = scanner.nextLine();
        System.out.print("Enter new description (Enter to keep current): ");
        String description = scanner.nextLine();

        if (name.trim().isEmpty()) {
            name = current.getName();
        }
        if (description.trim().isEmpty()) {
            description = current.getDescription();
        }

        boolean updated = productsService.update(
                new Products(id, name, description, categoryId, null, current.isArchived()));
        if (updated) {
            System.out.println("Product ID " + id + " updated successfully!");
        } else {
            System.out.println("No product updated (check ID or input).");
        }
    }

    public void promptSearchProducts() {
        System.out.println("\n=== SEARCH Products ===");
        System.out.print("Enter product name keyword: ");
        String keyword = scanner.nextLine();

        List<Products> matches = productsService.searchByName(keyword);
        printProductTable("\n----- Search Products (Keyword: '" + keyword.trim() + "') -----",
                matches, "No matching active products found.");
    }

    public void promptProductsByCategory() {
        System.out.println("\n=== VIEW Products By Category ===");
        int categoryId = promptForInt("Enter category ID: ");

        printProductTable("\n----- Active Products (Category ID: " + categoryId + ") -----",
                productsService.findActiveByCategory(categoryId), "No active products found in that category.");
    }

    public void promptArchiveProduct() {
        System.out.println("\n=== ARCHIVE Product ===");
        displayAllProducts();
        int id = promptForInt("Enter product ID to archive: ");

        boolean archived = productsService.archiveById(id);
        if (archived) {
            System.out.println("Product ID " + id + " archived successfully!");
        } else {
            System.out.println("No product archived (check ID or input).");
        }
    }

    public void promptRestoreProduct() {
        System.out.println("\n=== RESTORE Product ===");
        List<Products> archivedProducts = productsService.findAllArchived();
        printProductTable("\n----- Archived Products -----", archivedProducts, "No archived products found.");

        if (archivedProducts == null || archivedProducts.isEmpty()) {
            return;
        }

        int id = promptForInt("Enter product ID to restore: ");
        boolean restored = productsService.restoreById(id);
        if (restored) {
            System.out.println("Product ID " + id + " restored successfully!");
        } else {
            System.out.println("No archived product restored (check ID or input).");
        }
    }

    public void promptDeleteProduct() {
        System.out.println("\n=== DELETE Product ===");
        displayAllProducts();
        int id = promptForInt("Enter product ID to permanently delete: ");

        System.out.print("This cannot be undone. Type YES to confirm: ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equals("YES")) {
            System.out.println("Delete cancelled.");
            return;
        }

        boolean deleted = productsService.deleteById(id);
        if (deleted) {
            System.out.println("Product ID " + id + " deleted successfully!");
        } else {
            System.out.println("No product deleted (check ID or input).");
        }
    }

    private void printProductTable(String header, List<Products> products, String emptyMessage) {
        String border = "+------+----------------------+------------------+------------------------------+";

        System.out.println("\n" + header);
        System.out.println(border);
        System.out.printf("| %-4s | %-20s | %-16s | %-28s |%n", "ID", "Name", "Category", "Description");
        System.out.println(border);

        if (products == null || products.isEmpty()) {
            String message = (emptyMessage != null && !emptyMessage.isEmpty())
                    ? emptyMessage
                    : "No products found.";
            System.out.printf("| %-77s |%n", truncate(message, 77));
            System.out.println(border);
            return;
        }

        for (Products product : products) {
            String name = product.getName() != null ? product.getName() : "";
            String category = product.getCategoryName() != null ? product.getCategoryName() : "";
            String description = product.getDescription() != null ? product.getDescription() : "";

            System.out.printf("| %-4d | %-20s | %-16s | %-28s |%n",
                    product.getId(),
                    truncate(name, 20),
                    truncate(category, 16),
                    truncate(description, 28));
        }

        System.out.println(border);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= 3) {
            return text.substring(0, maxLength);
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private int promptForInt(String promptMessage) {
        while (true) {
            System.out.print(promptMessage);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    // Pressing Enter keeps the current number.
    private int promptIntWithDefault(String promptMessage, int current) {
        while (true) {
            System.out.print(promptMessage + " (Enter to keep " + current + "): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return current;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}