package com.mediamarktsaturn.azubi.core.product;

import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;

import java.util.List;
import java.util.Optional;

public interface SalesProductRepository {

    Optional<SalesProduct> getSalesProductByArticleNumber(Integer id);

    List<SalesProduct> findAll();

    SalesProduct save(SalesProduct product);

    void deleteById(Integer id);

    boolean existsById(Integer id);
}
