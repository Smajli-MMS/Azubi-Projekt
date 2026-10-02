package com.mediamarktsaturn.azubi.dto;

import java.util.List;

public class MarketStockOverviewDto {
    private Integer productId;
    private String productName;
    private Integer totalStock;
    private List<MarketStockDetailDto> marketStocks;

    public MarketStockOverviewDto() {
    }

    public MarketStockOverviewDto(Integer productId, String productName, Integer totalStock, List<MarketStockDetailDto> marketStocks) {
        this.productId = productId;
        this.productName = productName;
        this.totalStock = totalStock;
        this.marketStocks = marketStocks;
    }

    // Getters und Setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public List<MarketStockDetailDto> getMarketStocks() {
        return marketStocks;
    }

    public void setMarketStocks(List<MarketStockDetailDto> marketStocks) {
        this.marketStocks = marketStocks;
    }

    // Innere Klasse für Markt-Details
    public static class MarketStockDetailDto {
        private Integer marketId;
        private String marketName;
        private String location;
        private Integer stock;
        private boolean available;

        public MarketStockDetailDto() {
        }

        public MarketStockDetailDto(Integer marketId, String marketName, String location, Integer stock, boolean available) {
            this.marketId = marketId;
            this.marketName = marketName;
            this.location = location;
            this.stock = stock;
            this.available = available;
        }

        // Getters und Setters
        public Integer getMarketId() {
            return marketId;
        }

        public void setMarketId(Integer marketId) {
            this.marketId = marketId;
        }

        public String getMarketName() {
            return marketName;
        }

        public void setMarketName(String marketName) {
            this.marketName = marketName;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }
    }
}
