package com.mediamarktsaturn.azubi.stock;

import com.mediamarktsaturn.azubi.core.stock.StockRepository;
import com.mediamarktsaturn.azubi.core.stock.model.Stock;
import com.mediamarktsaturn.azubi.stock.repository.StockJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DefaultStockRepository implements StockRepository {

    private StockJpaRepository jpaRepository;

    public DefaultStockRepository(StockJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Stock> getAllStock() {
        return jpaRepository.findAll()
                .stream()
                .map(StockEntityMapper::mapToDomain)
                .toList();
    }

    @Override
    public Optional<Stock> getStockByProductId(Integer productId) {
        return jpaRepository.findByProductId(productId)
                .map(StockEntityMapper::mapToDomain);
    }

    @Override
    public List<Stock> getStockLessThan(Integer threshold) {
        return jpaRepository.findByStockLessThan(threshold)
                .stream()
                .map(StockEntityMapper::mapToDomain)
                .toList();
    }

    @Override
    public List<Stock> getStockGreaterThan(Integer threshold) {
        return jpaRepository.findByStockGreaterThan(threshold)
                .stream()
                .map(StockEntityMapper::mapToDomain)
                .toList();
    }
}