package com.mediamarktsaturn.azubi.market;

import com.mediamarktsaturn.azubi.core.market.model.Market;
import com.mediamarktsaturn.azubi.core.market.model.MarketStock;
import com.mediamarktsaturn.azubi.core.product.model.Category;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.generated.model.MarketApiResponseDTO;
import com.mediamarktsaturn.azubi.generated.model.ProductWithMarketStockDTO;
import java.math.BigDecimal;

public class MarketApiMapper {

    public static MarketApiResponseDTO convertToDto(Market market) {
        return new MarketApiResponseDTO()
                .marketId(market.getMarketId())
                .marketName(market.getMarketName())
                .location(market.getLocation())
                .postalCode(market.getPostalCode());
    }

    public static ProductWithMarketStockDTO convertToProductWithStockDto(
            MarketStock marketStock, SalesProduct product, Market market) {

        return new ProductWithMarketStockDTO()
                .productId(product.getId())
                .name(product.getName())
                .price(BigDecimal.valueOf(product.getPrice()))
                .productGroupId(mapCategoryToProductGroup(product.getCategory()))
                .stock(marketStock.getStock())
                .marketName(market.getMarketName())
                .marketId(market.getMarketId());
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
