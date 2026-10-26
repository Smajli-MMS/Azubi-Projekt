package com.mediamarktsaturn.azubi.market.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketstock", schema = "relin_uni2")
public class MarketStockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "marketstockid")
    private Integer marketStockId;

    @Column(name = "marketid")
    private Integer marketId;

    @Column(name = "productid")
    private Integer productId;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "lastchange")
    private LocalDateTime lastChange;

    // Keine @ManyToOne-Relation mehr! com.mediamarktsaturn.azubi.core.market.model.Market
    // ist eine reine Domain-Klasse ohne @Entity-Annotation - Hibernate würde beim
    // Start abstürzen, wenn man darauf eine JPA-Relation legt.
    // Market/Produkt werden über marketService.getMarketById()/getProduct() nachgeladen.

    public MarketStockEntity() {
    }

    public MarketStockEntity(Integer marketId, Integer productId, Integer stock) {
        this.marketId = marketId;
        this.productId = productId;
        this.stock = stock;
        this.lastChange = LocalDateTime.now();
    }

    public Integer getMarketStockId() {
        return marketStockId;
    }

    public void setMarketStockId(Integer marketStockId) {
        this.marketStockId = marketStockId;
    }

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
}
