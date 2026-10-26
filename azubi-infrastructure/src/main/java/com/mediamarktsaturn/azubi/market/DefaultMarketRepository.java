package com.mediamarktsaturn.azubi.market;

import com.mediamarktsaturn.azubi.core.market.MarketRepository;
import com.mediamarktsaturn.azubi.core.market.MarketStockRepository;
import com.mediamarktsaturn.azubi.core.market.model.Market;
import com.mediamarktsaturn.azubi.core.market.model.MarketStock;
import com.mediamarktsaturn.azubi.market.repository.MarketJpaRepository;
import com.mediamarktsaturn.azubi.market.repository.MarketStockJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DefaultMarketRepository implements MarketRepository, MarketStockRepository {

    private final MarketJpaRepository marketJpaRepository;
    private final MarketStockJpaRepository marketStockJpaRepository;

    public DefaultMarketRepository(MarketJpaRepository marketJpaRepository, MarketStockJpaRepository marketStockJpaRepository) {
        this.marketJpaRepository = marketJpaRepository;
        this.marketStockJpaRepository = marketStockJpaRepository;
    }

    @Override
    public List<Market> findAll() {
        return marketJpaRepository.findAll()
                .stream()
                .map(MarketEntityMapper::mapToDomain)
                .toList();
    }

    @Override
    public Optional<Market> findById(Integer marketId) {
        return marketJpaRepository.findById(marketId)
                .map(MarketEntityMapper::mapToDomain);
    }

    @Override
    public List<MarketStock> findByMarketId(Integer marketId) {
        return marketStockJpaRepository.findByMarketId(marketId)
                .stream()
                .map(MarketEntityMapper::mapToDomain)
                .toList();
    }

    @Override
    public List<MarketStock> findAvailableStockByMarket(Integer marketId) {
        return marketStockJpaRepository.findAvailableStockByMarket(marketId)
                .stream()
                .map(MarketEntityMapper::mapToDomain)
                .toList();
    }

    @Override
    public List<MarketStock> findAllStockForProduct(Integer productId) {
        return marketStockJpaRepository.findAllStockForProduct(productId)
                .stream()
                .map(MarketEntityMapper::mapToDomain)
                .toList();
    }

    @Override
    public Optional<MarketStock> findByMarketIdAndProductId(Integer marketId, Integer productId) {
        return marketStockJpaRepository.findByMarketIdAndProductId(marketId, productId)
                .map(MarketEntityMapper::mapToDomain);
    }
    
}
