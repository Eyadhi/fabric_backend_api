package com.example.fabric.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fabric.model.ProductMachine;

@Repository
public interface ProductMachineRepository extends JpaRepository<ProductMachine, Long> {
    
    // Find all product-machine combinations by completion status
    List<ProductMachine> findByIsComplete(int isComplete);
    
    // Find all product-machine combinations for a specific machine
    List<ProductMachine> findByMachineId(Long machineId);
    
    // Find all product-machine combinations for a specific product
    List<ProductMachine> findByProductId(Long productId);
    
    // Find running product on a specific machine
    ProductMachine findByMachineIdAndIsComplete(Long machineId, int isComplete);
    
    // Find specific product-machine combination by product, machine, and status
    ProductMachine findByProductIdAndMachineIdAndIsComplete(Long productId, Long machineId, int isComplete);
    
    // Find all product-machine combinations for a specific machine ordered by creation date
    List<ProductMachine> findByMachineIdOrderByCreatedAtDesc(Long machineId);
    
    // Custom query to get product-machine combinations with product and machine details
    @Query("SELECT pm FROM ProductMachine pm " +
           "JOIN FETCH pm.product p " +
           "JOIN FETCH pm.machine m " +
           "WHERE pm.isComplete = :isComplete " +
           "ORDER BY pm.createdAt DESC")
    List<ProductMachine> findByIsCompleteWithDetails(@Param("isComplete") int isComplete);
    
    // Custom query to get all product-machine combinations with details
    @Query("SELECT pm FROM ProductMachine pm " +
           "JOIN FETCH pm.product p " +
           "JOIN FETCH pm.machine m " +
           "ORDER BY pm.createdAt DESC")
    List<ProductMachine> findAllWithDetails();
}