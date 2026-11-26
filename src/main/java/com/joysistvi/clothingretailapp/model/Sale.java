package com.joysistvi.clothingretailapp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// class for representing sales db table
public class Sale {

    private int saleId;
    private int userId;
    private LocalDateTime saleDatetime;
    private BigDecimal totalAmount;
    private BigDecimal cashReceived;
    private BigDecimal changeAmount;
    private String status;

    public Sale() {
    }

    public Sale(int saleId, int userId, LocalDateTime saleDatetime, BigDecimal totalAmount, BigDecimal cashReceived, BigDecimal changeAmount, String status) {
        this.saleId = saleId;
        this.userId = userId;
        this.saleDatetime = saleDatetime;
        this.totalAmount = totalAmount;
        this.cashReceived = cashReceived;
        this.changeAmount = changeAmount;
        this.status = status;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getSaleDatetime() {
        return saleDatetime;
    }

    public void setSaleDatetime(LocalDateTime saleDatetime) {
        this.saleDatetime = saleDatetime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getCashReceived() {
        return cashReceived;
    }

    public void setCashReceived(BigDecimal cashReceived) {
        this.cashReceived = cashReceived;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
