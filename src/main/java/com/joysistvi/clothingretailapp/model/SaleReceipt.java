package com.joysistvi.clothingretailapp.model;

import java.util.List;

// class for displaying a saved sale and its items
public class SaleReceipt {
    private Sale sale;
    private List<SaleItem> items;

    public SaleReceipt() {
        this.items = List.of();
    }

    public SaleReceipt(Sale sale, List<SaleItem> items) {
        this.sale = sale;
        this.items = List.copyOf(items);
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = List.copyOf(items);
    }
}
