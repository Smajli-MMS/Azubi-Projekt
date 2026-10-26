package com.mediamarktsaturn.azubi.product.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;

public record UpdateProductApiRequest(
        String name,
        Integer productGroupId,
        @JsonFormat(shape = JsonFormat.Shape.NUMBER) BigDecimal price
) {}
