package com.mediamarktsaturn.azubi.core.product.dto;

import com.mediamarktsaturn.azubi.core.product.model.Category;

import java.math.BigDecimal;

public class UpdateProductDto {

    private final String name;
    private final Category category;
    private final BigDecimal price;

    public UpdateProductDto(String name, Category category, BigDecimal price) {
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }
}


