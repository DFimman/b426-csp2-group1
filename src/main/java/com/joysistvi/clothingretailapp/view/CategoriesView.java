package com.joysistvi.clothingretailapp.view;

import com.joysistvi.clothingretailapp.model.Categories;
import com.joysistvi.clothingretailapp.service.CategoriesService;

import java.util.List;
import java.util.Scanner;

public class CategoriesView {
    private final CategoriesService categoriesService;
    private final Scanner scanner;

    public CategoriesView(CategoriesService categoriesService, Scanner scanner) {
        this.categoriesService = categoriesService;
        this.scanner = scanner;
    }

    public void handleMenu() {
        while (true) {
            System.out.println("\n=== CATEGORIES MANAGEMENT ===");
            System.out.println("1. View All Active Categories");
            System.out.println("2. Create Category");
            System.out.println("3. Update Category");
            System.out.println("4. Search Categories");
            System.out.println("5. Archive Category");
            System.out.println("6. Restore Category");
            System.out.println("7. Delete Category");
            System.out.println("0. Back");

            int choice = promptForInt("Choose an option: ");
            switch (choice) {
                case 1:
                    displayAllCategories();
                    break;
                case 2:
                    promptCreateCategory();
                    break;
                case 3:
                    promptUpdateCategory();
                    break;
                case 4:
                    promptSearchCategories();
                    break;
                case 5:
                    promptArchiveCategory();
                    break;
                case 6:
                    promptRestoreCategory();
                    break;
                case 7:
                    promptDeleteCategory();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    public void displayAllCategories() {
        printCategoryTable("\n----- Active Categories -----",
                categoriesService.findAllActive(), "No active categories found.");
    }

    public void promptCreateCategory() {
        System.out.println("\n=== CREATE Category ===");
        System.out.print("Enter category name: ");
        String name = scanner.nextLine();
        System.out.print("Enter description (optional): ");
        String description = scanner.nextLine();

        boolean created = categoriesService.save(new Categories(0, name, description, false));
        if (created) {
            System.out.println("Category '" + name.trim() + "' created successfully!");
        } else {
            System.out.println("Failed to create category.");
        }
    }

    public void promptUpdateCategory() {
        System.out.println("\n=== UPDATE Category ===");
        displayAllCategories();
        int id = promptForInt("Enter category ID to update: ");

        Categories current = categoriesService.findById(id);
        if (current == null) {
            System.out.println("No category updated (check ID or input).");
            return;
        }

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

        boolean updated = categoriesService.update(new Categories(id, name, description, current.isArchived()));
        if (updated) {
            System.out.println("Category ID " + id + " updated successfully!");
        } else {
            System.out.println("No category updated (check ID or input).");
        }
    }

    public void promptSearchCategories() {
        System.out.println("\n=== SEARCH Categories ===");
        System.out.print("Enter category name keyword: ");
        String keyword = scanner.nextLine();

        List<Categories> matches = categoriesService.searchByName(keyword);
        printCategoryTable("\n----- Search Categories (Keyword: '" + keyword.trim() + "') -----",
                matches, "No matching active categories found.");
    }

    public void promptArchiveCategory() {
        System.out.println("\n=== ARCHIVE Category ===");
        displayAllCategories();
        int id = promptForInt("Enter category ID to archive: ");

        boolean archived = categoriesService.archiveById(id);
        if (archived) {
            System.out.println("Category ID " + id + " archived successfully!");
        } else {
            System.out.println("No category archived (check ID or input).");
        }
    }

    public void promptRestoreCategory() {
        System.out.println("\n=== RESTORE Category ===");
        List<Categories> archivedCategories = categoriesService.findAllArchived();
        printCategoryTable("\n----- Archived Categories -----", archivedCategories, "No archived categories found.");

        if (archivedCategories == null || archivedCategories.isEmpty()) {
            return;
        }

        int id = promptForInt("Enter category ID to restore: ");
        boolean restored = categoriesService.restoreById(id);
        if (restored) {
            System.out.println("Category ID " + id + " restored successfully!");
        } else {
            System.out.println("No archived category restored (check ID or input).");
        }
    }

    public void promptDeleteCategory() {
        System.out.println("\n=== DELETE Category ===");
        displayAllCategories();
        int id = promptForInt("Enter category ID to permanently delete: ");

        System.out.print("This cannot be undone. Type YES to confirm: ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equals("YES")) {
            System.out.println("Delete cancelled.");
            return;
        }

        boolean deleted = categoriesService.deleteById(id);
        if (deleted) {
            System.out.println("Category ID " + id + " deleted successfully!");
        } else {
            System.out.println("No category deleted (check ID or input).");
        }
    }

    private void printCategoryTable(String header, List<Categories> categories, String emptyMessage) {
        String border = "+------+----------------------+------------------------------+";

        System.out.println("\n" + header);
        System.out.println(border);
        System.out.printf("| %-4s | %-20s | %-28s |%n", "ID", "Name", "Description");
        System.out.println(border);

        if (categories == null || categories.isEmpty()) {
            String message = (emptyMessage != null && !emptyMessage.isEmpty())
                    ? emptyMessage
                    : "No categories found.";
            System.out.printf("| %-58s |%n", truncate(message, 58));
            System.out.println(border);
            return;
        }

        for (Categories category : categories) {
            String name = category.getName() != null ? category.getName() : "";
            String description = category.getDescription() != null ? category.getDescription() : "";

            System.out.printf("| %-4d | %-20s | %-28s |%n",
                    category.getId(),
                    truncate(name, 20),
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
}