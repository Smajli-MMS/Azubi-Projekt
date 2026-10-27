package com.mediamarktsaturn.azubi.core.market.model;

import java.time.LocalDateTime;

public class MarketStock {

    private Integer marketId;

    private Integer productId;

    private Integer stock;

    private LocalDateTime lastChange;

    public MarketStock() {
    }

    public MarketStock(Integer marketId, Integer productId, Integer stock) {
        this.marketId = marketId;
        this.productId = productId;
        this.stock = stock;
        this.lastChange = LocalDateTime.now();
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
