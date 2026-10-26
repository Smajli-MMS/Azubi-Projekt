package com.mediamarktsaturn.azubi.core.stock.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

public class Stock {

    private Integer productId;
    private Integer stock;
    private LocalDateTime lastChange;


    public Stock(Integer productId, Integer stock) {
        this.productId = productId;
        this.stock = stock;
        this.lastChange = LocalDateTime.now();
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
        this.lastChange = LocalDateTime.now();
    }

    public LocalDateTime getLastChange() {
        return lastChange;
    }

    public void setLastChange(LocalDateTime lastChange) {
        this.lastChange = lastChange;
    }
}
