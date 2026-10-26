package com.mediamarktsaturn.azubi.stock.repository;

import com.mediamarktsaturn.azubi.stock.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockJpaRepository extends JpaRepository<StockEntity, Integer> {

    Optional<StockEntity> findByProductId(Integer productId);

    List<StockEntity> findByStockLessThan(Integer threshold);

    List<StockEntity> findByStockGreaterThan(Integer threshold);
}