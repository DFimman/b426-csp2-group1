package com.joysistvi.clothingretailapp.view;
import com.joysistvi.clothingretailapp.model.SaleItem;
import java.util.List;

public class SaleItemView {

    // Displays the sale item details.
    public void display(List<SaleItem> items) {
        System.out.printf("%-8s | %-10s | %8s | %12s | %12s%n", "Line ID", "Variant", "Qty", "Unit Price", "Subtotal");
        if (items == null || items.isEmpty()) { System.out.println("No items found."); return; }
        for (SaleItem item : items) {
            System.out.printf("%-8d %-10d %8d %12s %12s%n",
                    item.getSaleItemId(), item.getVariantId(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal());
        }
    }
}
