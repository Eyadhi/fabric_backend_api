package com.example.fabric.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fabric.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByProductName(String productName);
    Product findByProductCode(String productCode);
    
    // Product-level completion status queries (aggregated from ProductMachine)
    List<Product> findByIsComplete(int isComplete);
}
