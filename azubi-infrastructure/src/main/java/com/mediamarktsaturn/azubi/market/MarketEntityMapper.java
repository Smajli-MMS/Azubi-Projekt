package com.mediamarktsaturn.azubi.market;

import com.mediamarktsaturn.azubi.core.market.model.Market;
import com.mediamarktsaturn.azubi.core.market.model.MarketStock;
import com.mediamarktsaturn.azubi.market.entity.MarketEntity;
import com.mediamarktsaturn.azubi.market.entity.MarketStockEntity;

public class MarketEntityMapper {

    static Market mapToDomain(MarketEntity entity) {
        Market market = new Market();
        market.setMarketId(entity.getMarketId());
        market.setMarketName(entity.getMarketName());
        market.setLocation(entity.getLocation());
        market.setPostalCode(entity.getPostalCode());
        market.setCreated(entity.getCreated());
        market.setUpdated(entity.getUpdated());
        return market;
    }

    static MarketStock mapToDomain(MarketStockEntity entity) {
        return new MarketStock(
                entity.getMarketId(),
                entity.getProductId(),
                entity.getStock()
        );

    }
}
