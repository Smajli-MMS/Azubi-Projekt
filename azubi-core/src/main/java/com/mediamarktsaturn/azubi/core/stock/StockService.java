package com.mediamarktsaturn.azubi.core.stock;

import com.mediamarktsaturn.azubi.core.stock.model.Stock;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockService {

    private StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<Stock> getAllStock() {
        return stockRepository.getAllStock();
    }

    public Optional<Stock> getStockByProductId(Integer productId) {
        return stockRepository.getStockByProductId(productId);
    }

    public List<Stock> getLowStock(Integer threshold) {
        return stockRepository.getStockLessThan(threshold);
    }

    public List<Stock> getAvailableStock() {
        return stockRepository.getStockGreaterThan(0);
    }
}