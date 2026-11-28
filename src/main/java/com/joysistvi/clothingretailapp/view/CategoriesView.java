package com.joysistvi.clothingretailapp.view;

import com.joysistvi.clothingretailapp.model.Categories;
import com.joysistvi.clothingretailapp.service.CategoriesService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class CategoriesView {
    private final CategoriesService categoriesService;
    private final Scanner scanner;
    private final ConsoleInput input;

    public CategoriesView(CategoriesService categoriesService, Scanner scanner) {
        this.categoriesService = categoriesService;
        this.scanner = scanner;
        this.input = new ConsoleInput(scanner);
    }

    // Displays and handles the category menu.
    public void handleMenu() {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n=== CATEGORIES MANAGEMENT ===");
            System.out.println("1. View All Active Categories");
            System.out.println("2. Create Category");
            System.out.println("3. Update Category");
            System.out.println("4. Search Categories");
            System.out.println("5. Archive Category");
            System.out.println("6. Restore Category");
            System.out.println("0. Back");
            try {
                int choice = input.integer("Choose an option: ");
                switch (choice) {
                    case 1 -> displayAllCategories();
                    case 2 -> promptCreateCategory();
                    case 3 -> promptUpdateCategory();
                    case 4 -> promptSearchCategories();
                    case 5 -> promptArchiveCategory();
                    case 6 -> promptRestoreCategory();
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

    public void displayAllCategories() {
        ConsoleOutput.operationHeader("VIEW ACTIVE CATEGORIES");
        printCategoryTable("\n----- Active Categories -----", categoriesService.findAllActive());
    }

    public void promptCreateCategory() {
        ConsoleOutput.operationHeader("CREATE CATEGORY");
        String name = input.text("Enter category name: ");
        String description = input.text("Enter description (optional): ");
        boolean created = categoriesService.save(new Categories(0, name, description, false));
        System.out.println(created ? "Category created successfully!" : "Failed to create category.");
    }

    public void promptUpdateCategory() {
        ConsoleOutput.operationHeader("UPDATE CATEGORY");
        printCategoryTable("\n----- Active Categories -----", categoriesService.findAllActive());
        int id = input.positive("Enter category ID to update: ");
        Categories current = categoriesService.findById(id);
        if (current == null) {
            System.out.println("Category not found.");
            return;
        }
        String name = input.text("Enter new name (Enter to keep current): ");
        String description = input.text("Enter new description (Enter to keep current): ");
        if (name.isBlank()) name = current.getName();
        if (description.isBlank()) description = current.getDescription();
        boolean updated = categoriesService.update(new Categories(id, name, description, current.isArchived()));
        System.out.println(updated ? "Category updated successfully!" : "No category updated.");
    }

    public void promptSearchCategories() {
        ConsoleOutput.operationHeader("SEARCH CATEGORIES");
        String keyword = input.text("Enter category name keyword: ");
        printCategoryTable("\n----- Search Results -----", categoriesService.searchByName(keyword));
    }

    public void promptArchiveCategory() {
        ConsoleOutput.operationHeader("ARCHIVE CATEGORY");
        printCategoryTable("\n----- Active Categories -----", categoriesService.findAllActive());
        int id = input.positive("Enter category ID to archive: ");
        boolean archived = categoriesService.archiveById(id);
        System.out.println(archived ? "Category archived successfully!" : "No category archived.");
    }

    public void promptRestoreCategory() {
        ConsoleOutput.operationHeader("RESTORE CATEGORY");
        printCategoryTable("\n----- Archived Categories -----", categoriesService.findAllArchived());
        int id = input.positive("Enter category ID to restore: ");
        boolean restored = categoriesService.restoreById(id);
        System.out.println(restored ? "Category restored successfully!" : "No category restored.");
    }

    public void promptDeleteCategory() {
        ConsoleOutput.operationHeader("DELETE CATEGORY (MAINTENANCE)");
        int id = input.positive("Enter category ID to permanently delete: ");
        String confirm = input.text("Type YES to confirm permanent deletion: ");
        if (!"YES".equals(confirm)) {
            System.out.println("Delete cancelled.");
            return;
        }
        boolean deleted = categoriesService.deleteById(id);
        System.out.println(deleted ? "Category deleted." : "Category could not be deleted.");
    }

    // Prints categories in a formatted table.
    private void printCategoryTable(String title, List<Categories> categories) {
        String border = "+------+----------------------+------------------------------+";
        System.out.println(title);
        System.out.println(border);
        System.out.printf("| %-4s | %-20s | %-28s |%n", "ID", "Name", "Description");
        System.out.println(border);
        if (categories == null || categories.isEmpty()) System.out.println("No categories found.");
        else for (Categories category : categories) {
            System.out.printf("| %-4d | %-20s | %-28s |%n", category.getId(),
                    ConsoleOutput.truncate(category.getName(), 20),
                    ConsoleOutput.truncate(category.getDescription(), 28));
        }
        System.out.println(border);
    }
}
