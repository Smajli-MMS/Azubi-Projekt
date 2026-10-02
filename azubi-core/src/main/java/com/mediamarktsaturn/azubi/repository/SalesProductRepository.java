package com.mediamarktsaturn.azubi.repository;


import ensar.relin.playground.boot.entity.SalesProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesProductRepository extends JpaRepository<SalesProduct, Integer> {

}

