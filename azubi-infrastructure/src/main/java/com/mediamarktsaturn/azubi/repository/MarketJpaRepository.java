package com.mediamarktsaturn.azubi.repository;

import com.mediamarktsaturn.azubi.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketJpaRepository extends JpaRepository<Market, Integer> {
}
