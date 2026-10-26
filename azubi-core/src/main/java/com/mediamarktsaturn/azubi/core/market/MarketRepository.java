package com.mediamarktsaturn.azubi.core.market;

import com.mediamarktsaturn.azubi.core.market.model.Market;

import java.util.List;
import java.util.Optional;

public interface MarketRepository {

    List<Market> findAll();

    Optional<Market> findById(Integer marketId);
}
