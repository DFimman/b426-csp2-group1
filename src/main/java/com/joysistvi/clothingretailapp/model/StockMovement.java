package com.joysistvi.clothingretailapp.model;

import java.time.LocalDateTime;

// class for representing stock_movements db table
public class StockMovement {

    private int movementId;
    private int variantId;
    private int userId;
    private Integer saleId;
    private String movementType;
    private int quantityChange;
    private String reason;
    private LocalDateTime createdAt;

    public StockMovement() {
    }

    public StockMovement(int movementId, int variantId, int userId, Integer saleId, String movementType, int quantityChange, String reason, LocalDateTime createdAt) {
        this.movementId = movementId;
        this.variantId = variantId;
        this.userId = userId;
        this.saleId = saleId;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public int getMovementId() {
        return movementId;
    }

    public void setMovementId(int movementId) {
        this.movementId = movementId;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
