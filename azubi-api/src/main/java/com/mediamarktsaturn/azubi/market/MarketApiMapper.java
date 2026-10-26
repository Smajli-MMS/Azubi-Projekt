package com.mediamarktsaturn.azubi.market;

import com.mediamarktsaturn.azubi.core.product.model.Category;
import com.mediamarktsaturn.azubi.core.market.model.Market;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.market.model.MarketApiResponse;
import com.mediamarktsaturn.azubi.market.model.ProductWithMarketStockDto;
import com.mediamarktsaturn.azubi.core.market.model.MarketStock;

import java.math.BigDecimal;

public class MarketApiMapper {

    public static MarketApiResponse convertToDto(Market market) {
        return new MarketApiResponse(
                market.getMarketId(),
                market.getMarketName(),
                market.getLocation(),
                market.getPostalCode()
        );
    }

    public static ProductWithMarketStockDto convertToProductWithStockDto(
            MarketStock marketStock, SalesProduct product, Market market) {

        return new ProductWithMarketStockDto(
                product.getId(),
                product.getName(),
                BigDecimal.valueOf(product.getPrice()),
                mapCategoryToProductGroup(product.getCategory()),
                marketStock.getStock(),
                market.getMarketName(),
                market.getMarketId()
        );
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
