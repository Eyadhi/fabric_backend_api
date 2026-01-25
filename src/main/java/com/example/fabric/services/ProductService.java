package com.example.fabric.services;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.fabric.model.Product;
import com.example.fabric.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductById(Long id) {
        return productRepository.findById(id).map(Collections::singletonList).orElse(Collections.emptyList());
    }
    
    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    // Product-level status queries (aggregated from ProductMachine entries)
    public List<Product> getProductsByStatus(int isComplete) {
        return productRepository.findByIsComplete(isComplete);
    }

    public Product updateProductStatus(Long productId, int isComplete) {
        return updateProductStatus(productId, isComplete, null);
    }

    public Product updateProductStatus(Long productId, int isComplete, LocalDate endDate) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        
        product.setIsComplete(isComplete);
        
        // Set the provided end date, or use default logic if not provided
        if (endDate != null) {
            product.setEndDate(endDate);
        } else {
            // Default behavior: set end date to today if completed, clear if in progress
            if (isComplete == 2) {
                product.setEndDate(LocalDate.now());
            } else if (isComplete == 1) {
                product.setEndDate(null);
            }
        }
        
        return productRepository.save(product);
    }

    // Legacy methods - these are now deprecated, use ProductMachineService instead
    @Deprecated
    public Product getRunningProductByMachineId(Long machineId) {
        throw new UnsupportedOperationException("This method is deprecated. Use ProductMachineService methods instead.");
    }

    @Deprecated
    public List<Product> getProductsByMachine(Long machineId) {
        throw new UnsupportedOperationException("This method is deprecated. Use ProductMachineService methods instead.");
    }
}
