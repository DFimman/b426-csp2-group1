package com.joysistvi.clothingretailapp.service;
import com.joysistvi.clothingretailapp.model.SaleItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class SaleTotals {
    private static final BigDecimal MAX = new BigDecimal("9999999999.99");

    private SaleTotals() {}

    // Validates and normalizes a monetary amount.
    public static BigDecimal money(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Money amount is required");
        if (amount.signum() < 0 || amount.compareTo(MAX) > 0)
            throw new IllegalArgumentException("Amount must be within DECIMAL(12,2) range");
        try { return amount.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException e) { throw new IllegalArgumentException("Only two decimal places allowed", e); }
    }

    // Calculates the total for one sale item.
    public static BigDecimal subtotal(int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        return money(money(unitPrice).multiply(BigDecimal.valueOf(quantity)));
    }

    // Calculates the combined sale total.
    public static BigDecimal total(List<SaleItem> items) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Cart cannot be empty");
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (SaleItem item : items) {
            if (item == null || !subtotal(item.getQuantity(), item.getUnitPrice()).equals(money(item.getSubtotal())))
                throw new IllegalArgumentException("Invalid item subtotal");
            sum = money(sum.add(item.getSubtotal()));
        }
        return sum;
    }

    // Calculates the change owed after payment.
    public static BigDecimal change(BigDecimal cash, BigDecimal total) {
        BigDecimal received = money(cash);
        BigDecimal due = money(total);
        if (received.compareTo(due) < 0) throw new IllegalArgumentException("Insufficient cash received");
        return money(received.subtract(due));
    }
}
