package com.mediamarktsaturn.azubi.market.repository;

import com.mediamarktsaturn.azubi.market.entity.MarketStockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketStockJpaRepository
        extends JpaRepository<MarketStockEntity, Integer> {

    List<MarketStockEntity> findByMarketId(Integer marketId);

    Optional<MarketStockEntity> findByMarketIdAndProductId(
            Integer marketId, Integer productId);

    @Query("""
            SELECT ms FROM MarketStockEntity ms
            WHERE ms.marketId = :marketId AND ms.stock > 0
            """)
    List<MarketStockEntity> findAvailableStockByMarket(
            @Param("marketId") Integer marketId);

    @Query("""
            SELECT ms FROM MarketStockEntity ms
            WHERE ms.productId = :productId
            """)
    List<MarketStockEntity> findAllStockForProduct(
            @Param("productId") Integer productId);
}