package com.mediamarktsaturn.azubi.product.repository;

import com.mediamarktsaturn.azubi.product.entity.SalesProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesProductJpaRepository extends JpaRepository<SalesProductEntity, Integer> {

}
