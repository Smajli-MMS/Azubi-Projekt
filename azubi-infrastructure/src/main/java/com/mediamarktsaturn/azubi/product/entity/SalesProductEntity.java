package com.mediamarktsaturn.azubi.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salesproduct", schema = "relin_uni2")  // ← Schema hinzugefügt
public class SalesProductEntity {
    @Id
    @Column(name = "productid")
    private Integer productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "productgroupid")
    private Integer productGroupId;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;

    // Rest bleibt gleich...
    public SalesProductEntity() {
    }

    public SalesProductEntity(String name, Integer productGroupId, BigDecimal price) {
        this.name = name;
        this.productGroupId = productGroupId;
        this.price = price;
        this.created = LocalDateTime.now();
        this.updated = LocalDateTime.now();
    }

    // Getters und Setters bleiben gleich...
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updated = LocalDateTime.now();
    }

    public Integer getProductGroupId() {
        return productGroupId;
    }

    public void setProductGroupId(Integer productGroupId) {
        this.productGroupId = productGroupId;
        this.updated = LocalDateTime.now();
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.updated = LocalDateTime.now();
    }

    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
        this.updated = LocalDateTime.now();
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
}
