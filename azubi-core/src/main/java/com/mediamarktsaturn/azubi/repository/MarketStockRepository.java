package com.mediamarktsaturn.azubi.repository;

import com.mediamarktsaturn.azubi.entity.MarketStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketStockRepository extends JpaRepository<MarketStock, Integer> {  // ← Long zu Integer

    List<MarketStock> findByMarketId(Integer marketId);

    Optional<MarketStock> findByMarketIdAndProductId(Integer marketId, Integer productId);

    @Query("SELECT ms FROM MarketStock ms WHERE ms.marketId = :marketId AND ms.stock > 0")
    List<MarketStock> findAvailableStockByMarket(@Param("marketId") Integer marketId);

    @Query("SELECT ms FROM MarketStock ms WHERE ms.productId = :productId")
    List<MarketStock> findAllStockForProduct(@Param("productId") Integer productId);
}
