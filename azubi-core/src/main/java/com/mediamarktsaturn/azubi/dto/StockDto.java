package com.mediamarktsaturn.azubi.dto;

import java.time.LocalDateTime;

public class StockDto {
    private Integer productId;
    private Integer stock;
    private LocalDateTime lastChange;

    // Konstruktoren
    public StockDto() {
    }

    public StockDto(Integer productId, Integer stock, LocalDateTime lastChange) {
        this.productId = productId;
        this.stock = stock;
        this.lastChange = lastChange;
    }

    // Getters und Setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDateTime getLastChange() {
        return lastChange;
    }

    public void setLastChange(LocalDateTime lastChange) {
        this.lastChange = lastChange;
    }

    @Override
    public String toString() {
        return "StockDto{" +
                "productId=" + productId +
                ", stock=" + stock +
                ", lastChange=" + lastChange +
                '}';
    }
}
