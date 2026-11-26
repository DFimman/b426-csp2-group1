package com.joysistvi.clothingretailapp.controller;

public class Product_Variant {
    private int variant_id;
    private int product_id;
    private String sku;
    private String size;
    private String color;
    private double unit_prize;
    private int stock_quantity;
    private int reorder_level;
    private boolean is_archived;

    public Product_Variant() {
    }

    public Product_Variant(int variant_id, int product_id, String sku, String size, String color, double unit_prize, int stock_quantity, int reorder_level, boolean is_archived) {
        this.variant_id = variant_id;
        this.product_id = product_id;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.unit_prize = unit_prize;
        this.stock_quantity = stock_quantity;
        this.reorder_level = reorder_level;
        this.is_archived = is_archived;
    }

    public Product_Variant(int product_id, String sku, String size, String color,
                           double unit_prize, int stock_quantity, int reorder_level) {
        this.product_id = product_id;
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.unit_prize = unit_prize;
        this.stock_quantity = stock_quantity;
        this.reorder_level = reorder_level;
        this.is_archived = false;
    }


    public int getVariant_id() {
        return variant_id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public String getSku() {
        return sku;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public double getUnit_prize() {
        return unit_prize;
    }

    public int getStock_quantity() {
        return stock_quantity;
    }

    public int getReorder_level() {
        return reorder_level;
    }

    public boolean isIs_archived() {
        return is_archived;
    }

    public void setVariant_id(int variant_id) {
        this.variant_id = variant_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setUnit_prize(double unit_prize) {
        this.unit_prize = unit_prize;
    }

    public void setStock_quantity(int stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public void setReorder_level(int reorder_level) {
        this.reorder_level = reorder_level;
    }

    public void setIs_archived(boolean is_archived) {
        this.is_archived = is_archived;
    }

    public boolean needsReorder() {
        return stock_quantity <= reorder_level;
    }

}
