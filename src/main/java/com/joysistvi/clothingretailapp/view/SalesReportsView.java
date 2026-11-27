package com.joysistvi.clothingretailapp.view;

import com.joysistvi.clothingretailapp.model.Product_Variant;
import com.joysistvi.clothingretailapp.model.Products;
import com.joysistvi.clothingretailapp.model.Sale;
import com.joysistvi.clothingretailapp.model.SaleItem;
import com.joysistvi.clothingretailapp.service.Product_VariantService;
import com.joysistvi.clothingretailapp.service.ProductsService;
import com.joysistvi.clothingretailapp.service.SaleService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.function.Function;

public class SalesReportsView {
    private final SaleService sales;
    private final Product_VariantService variants;
    private final ProductsService products;
    private final ConsoleInput input;
    private final Scanner scanner;

    // Reuses the existing sales and catalog services for basic reports.
    public SalesReportsView(SaleService sales, Product_VariantService variants,
                            ProductsService products, Scanner scanner) {
        this.sales = sales;
        this.variants = variants;
        this.products = products;
        this.scanner = scanner;
        this.input = new ConsoleInput(scanner);
    }

    // Displays all Admin reports and catches report errors without exiting the app.
    public void handleMenu(int adminId) {
        while (true) {
            ConsoleOutput.clearScreen();
            System.out.println("\n===== SALES REPORTS AND ANALYTICS =====");
            System.out.println("1. View All Completed Sales");
            System.out.println("2. Daily Sales Report");
            System.out.println("3. Weekly Sales Report (Monday - Sunday)");
            System.out.println("4. Monthly Sales Report");
            System.out.println("5. Top-Selling Products");
            System.out.println("6. Stock Summary / Most-Stocked Variants");
            System.out.println("0. Back");
            try {
                int choice = input.integer("Choose an option: ");
                if (choice == 0) return;
                switch (choice) {
                    case 1 -> allSales(adminId);
                    case 2 -> groupedSales(adminId, "Daily", date -> date.toLocalDate().toString());
                    case 3 -> groupedSales(adminId, "Weekly", SalesReportsView::weekLabel);
                    case 4 -> groupedSales(adminId, "Monthly", date -> YearMonth.from(date).toString());
                    case 5 -> topSellingProducts(adminId);
                    case 6 -> stockSummary();
                    default -> System.out.println("Invalid option. Please try again.");
                }
                if (!ConsoleOutput.pause(scanner)) return;
            } catch (NoSuchElementException | IllegalStateException e) {
                return;
            } catch (SQLException e) {
                System.out.println("Database Error: " + e.getMessage());
                if (!ConsoleOutput.pause(scanner)) return;
            } catch (RuntimeException e) {
                System.out.println("Operation failed: " + e.getMessage());
                if (!ConsoleOutput.pause(scanner)) return;
            }
        }
    }

    // Loads only completed sales from the Admin's authorized sales history.
    private List<Sale> completedSales(int adminId) throws SQLException {
        List<Sale> completed = new ArrayList<>();
        for (Sale sale : sales.history(adminId)) {
            if ("COMPLETED".equals(sale.getStatus())) completed.add(sale);
        }
        return completed;
    }

    // Prints all completed sales for the Admin.
    private void allSales(int adminId) throws SQLException {
        List<Sale> all = completedSales(adminId);
        if (all.isEmpty()) {
            System.out.println("No completed sales found.");
            return;
        }
        System.out.printf("%-8s %-10s %-21s %12s%n", "Sale ID", "Cashier", "Date / Time", "Total");
        for (Sale sale : all) {
            System.out.printf("%-8d %-10d %-21s %12s%n", sale.getSaleId(), sale.getUserId(),
                    sale.getSaleDatetime(), sale.getTotalAmount());
        }
    }

    // Groups sale counts and revenue by day, Monday-based week, or month.
    private void groupedSales(int adminId, String title, Function<LocalDateTime, String> period) throws SQLException {
        Map<String, Summary> groups = new TreeMap<>();
        for (Sale sale : completedSales(adminId)) {
            if (sale.getSaleDatetime() == null) continue;
            String key = period.apply(sale.getSaleDatetime());
            Summary summary = groups.computeIfAbsent(key, ignored -> new Summary());
            summary.count++;
            summary.revenue = summary.revenue.add(sale.getTotalAmount());
        }
        System.out.println("\n----- " + title + " Sales Report -----");
        if (groups.isEmpty()) {
            System.out.println("No completed sales found.");
            return;
        }
        System.out.printf("%-27s %-10s %-12s%n", "Period", "Sales", "Revenue");
        groups.forEach((key, value) ->
                System.out.printf("%-27s %-10d %-12s%n", key, value.count, value.revenue));
    }

    // Labels the reporting week as Monday through Sunday.
    private static String weekLabel(LocalDateTime timestamp) {
        LocalDate monday = timestamp.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday + " to " + monday.plusDays(6);
    }

    // Counts units sold per product across completed sale item receipts.
    private void topSellingProducts(int adminId) throws SQLException {
        Map<String, ProductSales> totals = new HashMap<>();
        for (Sale sale : completedSales(adminId)) {
            for (SaleItem item : sales.receipt(adminId, sale.getSaleId()).getItems()) {
                Product_Variant variant = variants.findById(item.getVariantId());
                int productId = variant == null ? 0 : variant.getProduct_id();
                String key = productId > 0 ? "PRODUCT-" + productId : "VARIANT-" + item.getVariantId();
                ProductSales total = totals.computeIfAbsent(key, ignored ->
                        new ProductSales(productId > 0 ? productName(productId) :
                                "Unknown variant #" + item.getVariantId()));
                total.quantity += item.getQuantity();
                total.revenue = total.revenue.add(item.getSubtotal());
            }
        }
        System.out.println("\n----- Top-Selling Products (by quantity) -----");
        if (totals.isEmpty()) {
            System.out.println("No completed sales found.");
            return;
        }
        System.out.printf("%-28s %10s %12s%n", "Product", "Units Sold", "Revenue");
        totals.values().stream()
                .sorted(Comparator.comparingInt((ProductSales result) -> result.quantity).reversed())
                .forEach(result -> System.out.printf("%-28s %10d %12s%n",
                        ConsoleOutput.truncate(result.name, 28), result.quantity, result.revenue));
        System.out.println("Product names reflect the current catalog labels.");
    }

    // Gets the current catalog name of a product, including archived products.
    private String productName(int productId) {
        Products product = products.findById(productId);
        return product == null ? "Unknown product #" + productId : product.getName();
    }

    // Shows total stock and the variants with the highest remaining quantity.
    private void stockSummary() {
        List<Product_Variant> all = new ArrayList<>(variants.getAllActiveVariants());
        int total = all.stream().mapToInt(Product_Variant::getStock_quantity).sum();
        long lowStock = all.stream().filter(Product_Variant::needsReorder).count();
        System.out.println("\n----- Stock Summary -----");
        System.out.println("Active variants: " + all.size());
        System.out.println("Units in stock: " + total);
        System.out.println("Low-stock variants: " + lowStock);
        System.out.println("\nMost-Stocked Variants:");
        if (all.isEmpty()) {
            System.out.println("No active variants found.");
            return;
        }
        System.out.printf("%-8s %-15s %-10s%n", "ID", "SKU", "Remaining");
        all.stream().sorted(Comparator.comparingInt(Product_Variant::getStock_quantity).reversed())
                .limit(10)
                .forEach(variant -> System.out.printf("%-8d %-15s %-10d%n",
                        variant.getVariant_id(), ConsoleOutput.truncate(variant.getSku(), 15),
                        variant.getStock_quantity()));
    }

    // Holds the total sales count and revenue for one reporting period.
    private static class Summary {
        int count;
        BigDecimal revenue = BigDecimal.ZERO;
    }

    // Holds the quantity and revenue sold for one product.
    private static class ProductSales {
        final String name;
        int quantity;
        BigDecimal revenue = BigDecimal.ZERO;

        ProductSales(String name) {
            this.name = name;
        }
    }
}
