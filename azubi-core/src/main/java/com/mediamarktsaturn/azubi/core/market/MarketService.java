package com.mediamarktsaturn.azubi.core.market;

import com.mediamarktsaturn.azubi.core.market.model.Market;
import com.mediamarktsaturn.azubi.core.product.SalesProductRepository;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.core.market.model.MarketStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MarketService {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private MarketStockRepository marketStockRepository;

    @Autowired
    private SalesProductRepository salesProductRepository;

    public List<Market> getAllMarkets() {
        return marketRepository.findAll();
    }

    public Optional<Market> getMarket(Integer marketId) {
        return marketRepository.findById(marketId);
    }

    public List<MarketStock> getStockByMarket(Integer marketId) {
        return marketStockRepository.findByMarketId(marketId);
    }

    public List<MarketStock> getAvailableStockByMarket(Integer marketId) {
        return marketStockRepository.findAvailableStockByMarket(marketId);
    }

    public List<MarketStock> getStockForProduct(Integer productId) {
        return marketStockRepository.findAllStockForProduct(productId);
    }

    public Optional<MarketStock> getSpecificStock(Integer marketId, Integer productId) {
        return marketStockRepository.findByMarketIdAndProductId(marketId, productId);
    }

    public Optional<SalesProduct> getProduct(Integer productId) {
        return salesProductRepository.getSalesProductByArticleNumber(productId);
    }
}
