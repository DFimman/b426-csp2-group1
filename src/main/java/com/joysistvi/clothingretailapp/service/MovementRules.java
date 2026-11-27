package com.joysistvi.clothingretailapp.service;

public final class MovementRules {
    private MovementRules() {}

    public static void validateManual(String type, int delta, String reason) {
        if (!"RESTOCK".equals(type) && !"ADJUSTMENT".equals(type))
            throw new IllegalArgumentException("Manual movements must be RESTOCK or ADJUSTMENT");
        if (delta == 0 || ("RESTOCK".equals(type) && delta < 0))
            throw new IllegalArgumentException("Invalid quantity change for " + type);
        if (reason == null || reason.isBlank() || reason.trim().length() > 255)
            throw new IllegalArgumentException("Reason is required (up to 255 characters)");
    }

    public static int nextStock(int current, int delta) {
        int result;
        try { result = Math.addExact(current, delta); }
        catch (ArithmeticException e) { throw new IllegalArgumentException("Stock exceeds integer range", e); }
        if (result < 0) throw new IllegalArgumentException("Insufficient stock");
        return result;
    }
}
