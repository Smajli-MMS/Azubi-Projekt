package com.mediamarktsaturn.azubi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketstock", schema = "relin_uni2")
public class MarketStock {
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

    // Nur Relation auf Market (liegt ebenfalls in azubi-core, kein Modul-Konflikt)
    @ManyToOne
    @JoinColumn(name = "marketid", insertable = false, updatable = false)
    private Market market;

    // Keine Relation auf SalesProductEntity! Die liegt in azubi-infrastructure.
    // core darf infrastructure nicht kennen (würde zirkuläre Modul-Abhängigkeit erzeugen).
    // Produktdaten werden im MarketController manuell über productId nachgeladen.

    public MarketStock() {
    }

    public MarketStock(Integer marketId, Integer productId, Integer stock) {
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

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }
}
