package com.mediamarktsaturn.azubi.core.stock;

import com.mediamarktsaturn.azubi.core.stock.model.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {

    List<Stock> getAllStock();

    Optional<Stock> getStockByProductId(Integer productId);

    List<Stock> getStockLessThan(Integer threshold);

    List<Stock> getStockGreaterThan(Integer threshold);
}