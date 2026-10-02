package com.mediamarktsaturn.azubi;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesProduct extends Product {
    private BigDecimal price = BigDecimal.ZERO;
    private LocalDateTime created;
    private LocalDateTime updated;

    public SalesProduct(int id, String name, Category category) {
        super(id, name, category);
        this.created = LocalDateTime.now();
        this.updated = LocalDateTime.now();
    }

    public SalesProduct(int id, String name, Category category, double price) {
        super(id, name, category);
        this.price = BigDecimal.valueOf(price);
        this.created = LocalDateTime.now();
        this.updated = LocalDateTime.now();
    }

    // Leerer Konstruktor für JSON
    public SalesProduct() {
        super();
        this.created = LocalDateTime.now();
        this.updated = LocalDateTime.now();
    }

    public double getPrice() {
        return price.doubleValue();
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.updated = LocalDateTime.now(); // Updated setzen bei Änderung
    }

    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
        this.updated = LocalDateTime.now(); // Updated setzen bei Änderung
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

    // Updated automatisch setzen bei Name/Category Änderungen
    @Override
    public void setName(String name) {
        super.setName(name);
        this.updated = LocalDateTime.now();
    }

    @Override
    public void setCategory(Category category) {
        super.setCategory(category);
        this.updated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "SalesProduct{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", category=" + getCategory() +
                ", price=" + price +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }
}
