package com.mediamarktsaturn.azubi.stock;

import com.mediamarktsaturn.azubi.core.stock.model.Stock;
import com.mediamarktsaturn.azubi.stock.entity.StockEntity;

public class StockEntityMapper {

    static Stock mapToDomain(StockEntity entity) {

        Stock stock = new Stock(
                entity.getProductId(),
                entity.getStock()
        );

        stock.setLastChange(entity.getLastChange());

        return stock;
    }
}