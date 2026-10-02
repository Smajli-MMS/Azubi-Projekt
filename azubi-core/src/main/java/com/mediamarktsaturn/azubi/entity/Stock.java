package com.mediamarktsaturn.azubi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock", schema = "relin_uni2")  // ✅ Schema hinzugefügt
public class Stock {
    @Id
    @Column(name = "productid")
    private Integer productId;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "lastchange")
    private LocalDateTime lastChange;

    public Stock() {
    }

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
