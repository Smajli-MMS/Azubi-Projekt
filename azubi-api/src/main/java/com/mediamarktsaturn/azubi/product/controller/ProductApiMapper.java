package com.mediamarktsaturn.azubi.product.controller;

import com.mediamarktsaturn.azubi.core.product.dto.UpdateProductDto;
import com.mediamarktsaturn.azubi.core.product.model.Category;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.generated.model.ProductApiDTO;
import com.mediamarktsaturn.azubi.generated.model.UpdateProductApiRequestDTO;
import java.math.BigDecimal;

public class ProductApiMapper {

    public static ProductApiDTO convertToDto(SalesProduct entity) {
        Integer groupId = entity.getCategory().ordinal();
        ProductApiDTO dto = new ProductApiDTO()
                .productId(entity.getId())
                .name(entity.getName())
                .productGroupId(groupId)
                .price(BigDecimal.valueOf(entity.getPrice()))
                .created(entity.getCreated())
                .updated(entity.getUpdated())
                .articleNumber(generateArticleNumber(entity.getId(), groupId));
        setStockQuantity(dto, null);
        return dto;
    }

    public static SalesProduct convertToDomain(ProductApiDTO dto) {
        var product = new SalesProduct();
        product.setId(dto.getProductId() != null ? dto.getProductId() : 0);
        product.setName(dto.getName());
        product.setCategory(mapProductGroupToCategory(dto.getProductGroupId()));
        product.setPrice(dto.getPrice() != null ? dto.getPrice().doubleValue() : 0.0);
        return product;
    }

    public static UpdateProductDto convertToDomain(UpdateProductApiRequestDTO request){
        return new UpdateProductDto(
                request.getName(),
                mapProductGroupToCategory(request.getProductGroupId()),
                request.getPrice()
        );
    }

    public static void setStockQuantity(ProductApiDTO dto, Integer quantity) {
        dto.setStockQuantity(quantity);
        dto.setStockStatus(quantity == null || quantity <= 0 ? "OUT_OF_STOCK"
                : quantity <= 5 ? "LOW_STOCK" : "AVAILABLE");
    }

    private static String generateArticleNumber(Integer productId, Integer productGroupId) {
        if (productId == null || productGroupId == null) {
            return "XX-0000000";
        }
        String prefix = switch (productGroupId) {
            case 1 -> "SP";
            case 2 -> "GM";
            case 3 -> "PC";
            case 4 -> "WM";
            default -> "XX";
        };
        return String.format("%s-%07d", prefix, productId);
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
