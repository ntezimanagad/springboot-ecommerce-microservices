package com.gad.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gad.product_service.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
