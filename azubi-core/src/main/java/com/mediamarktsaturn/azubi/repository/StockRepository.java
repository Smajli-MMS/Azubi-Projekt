package com.mediamarktsaturn.azubi.repository;

import com.mediamarktsaturn.azubi.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Integer> {

    Optional<Stock> findByProductId(Integer productId);

    // Produkte mit Bestand unter einem bestimmten Wert
    List<Stock> findByStockLessThan(Integer threshold);

    // Produkte mit verfügbarem Bestand
    List<Stock> findByStockGreaterThan(Integer minStock);

    // Custom Query: Alle Produkte ohne Bestand
    @Query("SELECT s FROM Stock s WHERE s.stock = 0")
    List<Stock> findOutOfStockProducts();

    // Custom Query: Bestand nach Produktgruppe
    @Query("SELECT s FROM Stock s JOIN SalesProduct sp ON s.productId = sp.productId WHERE sp.productGroupId = :groupId")
    List<Stock> findStockByProductGroup(@Param("groupId") Integer productGroupId);
}
