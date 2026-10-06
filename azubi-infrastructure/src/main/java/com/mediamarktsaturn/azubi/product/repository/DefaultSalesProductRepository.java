package com.mediamarktsaturn.azubi.product.repository;

import com.mediamarktsaturn.azubi.core.product.SalesProductRepository;
import com.mediamarktsaturn.azubi.core.product.model.SalesProduct;
import com.mediamarktsaturn.azubi.product.SalesProductEntityMapper;
import com.mediamarktsaturn.azubi.product.entity.SalesProductEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DefaultSalesProductRepository implements SalesProductRepository {

    private final SalesProductJpaRepository jpaRepository;

    public DefaultSalesProductRepository(SalesProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SalesProduct> getSalesProductByArticleNumber(Integer id) {
        return jpaRepository.findById(id).map(SalesProductEntityMapper::convertToSalesProduct);
    }

    @Override
    public List<SalesProduct> findAll() {
        return jpaRepository.findAll().stream()
                .map(SalesProductEntityMapper::convertToSalesProduct)
                .toList();
    }

    @Override
    public SalesProduct save(SalesProduct product) {
        SalesProductEntity entity = SalesProductEntityMapper.convertToEntity(product);
        SalesProductEntity saved = jpaRepository.save(entity);
        return SalesProductEntityMapper.convertToSalesProduct(saved);
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return jpaRepository.existsById(id);
    }
}
