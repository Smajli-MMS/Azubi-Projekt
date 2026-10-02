package com.mediamarktsaturn.azubi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketstock", schema = "relin_uni2")
public class MarketStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "marketstockid")
    private Integer marketStockId;  // ← Von Long zu Integer geändert

    @Column(name = "marketid")
    private Integer marketId;

    @Column(name = "productid")
    private Integer productId;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "lastchange")
    private LocalDateTime lastChange;

    // JPA Relationships
    @ManyToOne
    @JoinColumn(name = "marketid", insertable = false, updatable = false)
    private Market market;

    @ManyToOne
    @JoinColumn(name = "productid", insertable = false, updatable = false)
    private SalesProduct product;

    // Konstruktoren
    public MarketStock() {
    }

    public MarketStock(Integer marketId, Integer productId, Integer stock) {
        this.marketId = marketId;
        this.productId = productId;
        this.stock = stock;
        this.lastChange = LocalDateTime.now();
    }

    // Getters und Setters - NUR marketStockId geändert
    public Integer getMarketStockId() {
        return marketStockId;
    }

    public void setMarketStockId(Integer marketStockId) {
        this.marketStockId = marketStockId;
    }

    // Rest bleibt komplett gleich
    public Integer getMarketId() {
        return marketId;
    }

    public void setMarketId(Integer marketId) {
        this.marketId = marketId;
    }

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

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public SalesProduct getProduct() {
        return product;
    }

    public void setProduct(SalesProduct product) {
        this.product = product;
    }
}

