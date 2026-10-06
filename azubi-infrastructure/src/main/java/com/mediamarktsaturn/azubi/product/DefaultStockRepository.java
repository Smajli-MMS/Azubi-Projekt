package com.mediamarktsaturn.azubi.product;

import com.mediamarktsaturn.azubi.core.product.StockRepository;
import com.mediamarktsaturn.azubi.entity.Stock;
import com.mediamarktsaturn.azubi.repository.StockJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DefaultStockRepository implements StockRepository {

    private final StockJpaRepository jpaRepository;

    public DefaultStockRepository(StockJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Stock> findByProductId(Integer productId) {
        return jpaRepository.findByProductId(productId);
    }
}
