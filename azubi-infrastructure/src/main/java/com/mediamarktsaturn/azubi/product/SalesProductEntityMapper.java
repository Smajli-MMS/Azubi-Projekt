package com.mediamarktsaturn.azubi.product;

import com.mediamarktsaturn.azubi.Category;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.product.entity.SalesProductEntity;

import java.math.BigDecimal;

public class SalesProductEntityMapper {

    public static SalesProduct convertToSalesProduct(SalesProductEntity entity) {
        var salesProduct = new SalesProduct();
        salesProduct.setId(entity.getProductId());
        salesProduct.setName(entity.getName());
        salesProduct.setCategory(mapProductGroupToCategory(entity.getProductGroupId()));
        salesProduct.setPrice(entity.getPrice() != null ? entity.getPrice().doubleValue() : 0.0);
        salesProduct.setCreated(entity.getCreated());
        salesProduct.setUpdated(entity.getUpdated());
        return salesProduct;
    }

    public static SalesProductEntity convertToEntity(SalesProduct product) {
        var entity = new SalesProductEntity();
        entity.setProductId(product.getId());
        entity.setName(product.getName());
        entity.setProductGroupId(mapCategoryToProductGroup(product.getCategory()));
        entity.setPrice(BigDecimal.valueOf(product.getPrice()));
        entity.setCreated(product.getCreated());
        entity.setUpdated(product.getUpdated());
        return entity;
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

    private static Integer mapCategoryToProductGroup(Category category) {
        if (category == null) {
            return 1;
        }
        return switch (category) {
            case SMARTPHONE -> 1;
            case TV -> 2;
            case WASHINGMASHINE -> 4;
        };
    }
}
