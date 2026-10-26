package com.mediamarktsaturn.azubi.core.market;

import com.mediamarktsaturn.azubi.core.market.model.MarketStock;

import java.util.List;
import java.util.Optional;

public interface MarketStockRepository {

    List<MarketStock> findByMarketId(Integer marketId);

    List<MarketStock> findAvailableStockByMarket(Integer marketId);

    List<MarketStock> findAllStockForProduct(Integer productId);

    Optional<MarketStock> findByMarketIdAndProductId(Integer marketId, Integer productId);
}
