package com.example.fabric.services;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.fabric.exceptions.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> getProductsByStatus(int isComplete) {
        return productRepository.findByIsComplete(isComplete);
    }

    public Product updateProductStatus(Long productId, int isComplete) {
        return updateProductStatus(productId, isComplete, null);
    }

    public Product updateProductStatus(Long productId, int isComplete, LocalDate endDate) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        product.setIsComplete(isComplete);

        if (endDate != null) {
            product.setEndDate(endDate);
        } else {
            if (isComplete == 2) {
                product.setEndDate(LocalDate.now());
            } else if (isComplete == 1) {
                product.setEndDate(null);
            }
        }

        return productRepository.save(product);
    }

    @Deprecated
    public Product getRunningProductByMachineId(Long machineId) {
        throw new UnsupportedOperationException("Deprecated. Use ProductMachineService instead.");
    }

    @Deprecated
    public List<Product> getProductsByMachine(Long machineId) {
        throw new UnsupportedOperationException("Deprecated. Use ProductMachineService instead.");
    }
}
