package com.mediamarktsaturn.azubi.stock.model;

import java.time.LocalDateTime;

public record StockApiResponse(
        Integer productId,
        Integer stock,
        LocalDateTime lastChange
) {}