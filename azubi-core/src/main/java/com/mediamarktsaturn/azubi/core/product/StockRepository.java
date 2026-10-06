package com.mediamarktsaturn.azubi.core.product;

import com.mediamarktsaturn.azubi.entity.Stock;

import java.util.Optional;

public interface StockRepository {

    Optional<Stock> findByProductId(Integer productId);
}
