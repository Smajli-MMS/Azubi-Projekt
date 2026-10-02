package com.mediamarktsaturn.azubi.repository;

import com.mediamarktsaturn.azubi.entity.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepository extends JpaRepository<Market, Integer> {
}
