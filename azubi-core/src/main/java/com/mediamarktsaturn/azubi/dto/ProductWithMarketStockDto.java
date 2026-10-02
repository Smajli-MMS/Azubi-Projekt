package com.mediamarktsaturn.azubi.dto;

import java.math.BigDecimal;

public class ProductWithMarketStockDto {
    private Integer productId;
    private String name;
    private BigDecimal price;
    private Integer productGroupId;
    private Integer stock;
    private String marketName;
    private Integer marketId;

    public ProductWithMarketStockDto() {
    }

    public ProductWithMarketStockDto(Integer productId, String name, BigDecimal price,
                                     Integer productGroupId, Integer stock,
                                     String marketName, Integer marketId) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.productGroupId = productGroupId;
        this.stock = stock;
        this.marketName = marketName;
        this.marketId = marketId;
    }

    // Getters und Setters
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
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getProductGroupId() {
        return productGroupId;
    }

    public void setProductGroupId(Integer productGroupId) {
        this.productGroupId = productGroupId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public Integer getMarketId() {
        return marketId;
    }

    public void setMarketId(Integer marketId) {
        this.marketId = marketId;
    }
}
