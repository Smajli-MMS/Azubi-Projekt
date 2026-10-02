package com.mediamarktsaturn.azubi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDto {
    private Integer productId;
    private String articleNumber;
    private String name;
    private Integer productGroupId;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private BigDecimal price;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime created;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updated;

    @JsonProperty("stockQuantity")
    private Integer stockQuantity;

    // ✅ Zusätzliche Stock-Informationen
    private LocalDateTime stockLastChange;
    private String stockStatus;  // "AVAILABLE", "LOW_STOCK", "OUT_OF_STOCK"

    // Konstruktoren
    public ProductDto() {
        updateStockStatus(); // Initialer Status
    }

    public ProductDto(Integer productId, String name, Integer productGroupId,
                      BigDecimal price, LocalDateTime created, LocalDateTime updated) {
        this.productId = productId;
        this.name = name;
        this.productGroupId = productGroupId;
        this.price = price;
        this.created = created;
        this.updated = updated;
        this.articleNumber = generateArticleNumber(productId, productGroupId);
        updateStockStatus();
    }

    // ✅ Vollständiger Konstruktor mit Stock
    public ProductDto(Integer productId, String name, Integer productGroupId,
                      BigDecimal price, LocalDateTime created, LocalDateTime updated,
                      Integer stockQuantity, LocalDateTime stockLastChange) {
        this(productId, name, productGroupId, price, created, updated);
        this.stockQuantity = stockQuantity;
        this.stockLastChange = stockLastChange;
        updateStockStatus();
    }

    // ✅ Verbesserte Artikelnummer-Generierung
    private String generateArticleNumber(Integer productId, Integer productGroupId) {
        if (productId == null || productGroupId == null) {
            return "XX-0000000";
        }

        String prefix = switch (productGroupId) {
            case 1 -> "SP"; // Smartphone
            case 2 -> "GM"; // Gaming
            case 3 -> "PC"; // PCs
            case 4 -> "WM"; // Washing Machines
            default -> "XX"; // Unknown
        };
        return String.format("%s-%07d", prefix, productId);
    }

    // ✅ Stock-Status automatisch aktualisieren
    private void updateStockStatus() {
        if (stockQuantity == null || stockQuantity <= 0) {
            this.stockStatus = "OUT_OF_STOCK";
        } else if (stockQuantity <= 5) {
            this.stockStatus = "LOW_STOCK";
        } else {
            this.stockStatus = "AVAILABLE";
        }
    }

    // Getters und Setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
        this.articleNumber = generateArticleNumber(productId, productGroupId);
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProductGroupId() {
        return productGroupId;
    }

    public void setProductGroupId(Integer productGroupId) {
        this.productGroupId = productGroupId;
        this.articleNumber = generateArticleNumber(productId, productGroupId);
    }

    // ✅ Verbesserte Preis-Methoden
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public void setPrice(String price) {
        try {
            this.price = new BigDecimal(price);
        } catch (NumberFormatException e) {
            this.price = BigDecimal.ZERO;
        }
    }

    // ✅ Frontend-freundliche Preis-Methoden
    @JsonIgnore
    public double getPriceAsDouble() {
        return price != null ? price.doubleValue() : 0.0;
    }

    @JsonIgnore
    public String getPriceFormatted() {
        return price != null ? String.format("€%.2f", price.doubleValue()) : "€0.00";
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

    // ✅ Verbesserte Stock-Methoden
    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        updateStockStatus(); // Status automatisch aktualisieren
    }

    public LocalDateTime getStockLastChange() {
        return stockLastChange;
    }

    public void setStockLastChange(LocalDateTime stockLastChange) {
        this.stockLastChange = stockLastChange;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    // ✅ Hilfsmethoden für Stock-Prüfungen
    @JsonIgnore
    public boolean isAvailable() {
        return stockQuantity != null && stockQuantity > 0;
    }

    @JsonIgnore
    public boolean isLowStock() {
        return "LOW_STOCK".equals(stockStatus);
    }

    @JsonIgnore
    public boolean isOutOfStock() {
        return "OUT_OF_STOCK".equals(stockStatus);
    }

    @JsonIgnore
    public boolean hasStock(int requiredQuantity) {
        return stockQuantity != null && stockQuantity >= requiredQuantity;
    }

    // ✅ Produktkategorie als String
    @JsonIgnore
    public String getCategoryName() {
        if (productGroupId == null) return "Unbekannt";

        return switch (productGroupId) {
            case 1 -> "Smartphones";
            case 2 -> "Gaming";
            case 3 -> "Computer";
            case 4 -> "Haushaltsgeräte";
            default -> "Sonstige";
        };
    }

    // ✅ Validation-Methoden
    @JsonIgnore
    public boolean isValid() {
        return productId != null &&
                name != null && !name.trim().isEmpty() &&
                productGroupId != null &&
                price != null && price.compareTo(BigDecimal.ZERO) >= 0;
    }

    // ✅ Copy-Methoden für Updates
    public void updateFrom(ProductDto other) {
        if (other.name != null) this.name = other.name;
        if (other.price != null) this.price = other.price;
        if (other.productGroupId != null) {
            this.productGroupId = other.productGroupId;
            this.articleNumber = generateArticleNumber(productId, productGroupId);
        }
        if (other.stockQuantity != null) {
            this.stockQuantity = other.stockQuantity;
            updateStockStatus();
        }
        this.updated = LocalDateTime.now();
    }

    // ✅ Builder-Pattern für einfache Erstellung
    public static class Builder {
        private ProductDto dto = new ProductDto();

        public Builder id(Integer productId) {
            dto.setProductId(productId);
            return this;
        }

        public Builder name(String name) {
            dto.setName(name);
            return this;
        }

        public Builder productGroup(Integer productGroupId) {
            dto.setProductGroupId(productGroupId);
            return this;
        }

        public Builder price(BigDecimal price) {
            dto.setPrice(price);
            return this;
        }

        public Builder price(double price) {
            dto.setPrice(price);
            return this;
        }

        public Builder stock(Integer stock) {
            dto.setStockQuantity(stock);
            return this;
        }

        public Builder stockLastChange(LocalDateTime lastChange) {
            dto.setStockLastChange(lastChange);
            return this;
        }

        public ProductDto build() {
            dto.updateStockStatus();
            return dto;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "ProductDto{" +
                "productId=" + productId +
                ", articleNumber='" + articleNumber + '\'' +
                ", name='" + name + '\'' +
                ", productGroupId=" + productGroupId +
                ", categoryName='" + getCategoryName() + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", stockStatus='" + stockStatus + '\'' +
                ", available=" + isAvailable() +
                ", created=" + created +
                ", updated=" + updated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDto that = (ProductDto) o;
        return productId != null && productId.equals(that.productId);
    }

    @Override
    public int hashCode() {
        return productId != null ? productId.hashCode() : 0;
    }
}
