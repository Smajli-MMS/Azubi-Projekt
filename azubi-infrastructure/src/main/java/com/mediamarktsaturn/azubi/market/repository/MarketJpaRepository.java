package com.mediamarktsaturn.azubi.market.repository;

import com.mediamarktsaturn.azubi.market.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketJpaRepository extends JpaRepository<MarketEntity, Integer> {
}
