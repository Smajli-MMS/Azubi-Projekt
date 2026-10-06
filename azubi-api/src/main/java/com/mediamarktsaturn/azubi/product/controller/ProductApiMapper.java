package com.mediamarktsaturn.azubi.product.controller;

import com.mediamarktsaturn.azubi.Category;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.product.model.ProductApiDto;

import java.math.BigDecimal;

public class ProductApiMapper {

    public static ProductApiDto convertToDto(SalesProduct entity) {
        return new ProductApiDto(
                entity.getId(),
                entity.getName(),
                entity.getCategory().ordinal(),
                BigDecimal.valueOf(entity.getPrice()),
                entity.getCreated(),
                entity.getUpdated()
        );
    }

    public static SalesProduct convertToDomain(ProductApiDto dto) {
        var product = new SalesProduct();
        product.setId(dto.getProductId() != null ? dto.getProductId() : 0);
        product.setName(dto.getName());
        product.setCategory(mapProductGroupToCategory(dto.getProductGroupId()));
        product.setPrice(dto.getPrice() != null ? dto.getPrice().doubleValue() : 0.0);
        return product;
    }

    private static Category mapProductGroupToCategory(Integer productGroupId) {
        if (productGroupId == null) {
            return Category.SMARTPHONE;
        }
        return switch (productGroupId) {
            case 1 -> Category.SMARTPHONE;
            case 2 -> Category.TV;
            case 4 -> Category.WASHINGMASHINE;
            default -> Category.SMARTPHONE;
        };
    }
}
