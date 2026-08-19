package com.example.fabric.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.AddMultiMachineProductDto;
import com.example.fabric.dto.ProductMachineDto;
import com.example.fabric.dto.ProductWithMachinesDto;
import com.example.fabric.dto.UpdateProductDto;
import com.example.fabric.dto.UpdateProductMachineCompletionDto;
import com.example.fabric.model.Product;
import com.example.fabric.model.ProductMachine;
import com.example.fabric.services.ProductMachineService;
import com.example.fabric.services.ProductService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class ProductController {

    private final ProductService productService;
    private final ProductMachineService productMachineService;

    @GetMapping("/getProduct")
    public List<Product> getProduct(@RequestParam(value = "id", required = false) Long id) {
        return (id != null) ? productService.getProductById(id) : productService.getAllProducts();
    }

    @GetMapping("/getProductMachines")
    public ResponseEntity<?> getAllProductMachines() {
        List<ProductMachineDto> productMachines = productMachineService.getAllProductMachines();
        return ResponseUtil.createSuccessResponse(productMachines);
    }

    @GetMapping("/getRunningProducts")
    public ResponseEntity<?> getRunningProducts() {
        List<ProductWithMachinesDto> running = productMachineService.getProductsWithMachinesByStatus(1);
        return ResponseUtil.createSuccessResponse(running);
    }

    @GetMapping("/getCompletedProducts")
    public ResponseEntity<?> getCompletedProducts() {
        List<ProductWithMachinesDto> completed = productMachineService.getProductsWithMachinesByStatus(2);
        return ResponseUtil.createSuccessResponse(completed);
    }

    @GetMapping("/getProductStats")
    public ResponseEntity<?> getProductStats() {
        int runningCount   = productService.getProductsByStatus(1).size();
        int completedCount = productService.getProductsByStatus(2).size();

        Map<String, Integer> stats = new HashMap<>();
        stats.put("running",   runningCount);
        stats.put("completed", completedCount);
        stats.put("total",     runningCount + completedCount);

        return ResponseUtil.createSuccessResponse(stats);
    }

    @PostMapping("/addProductsToMultipleMachines")
    public ResponseEntity<?> saveProductToMultipleMachines(@RequestBody AddMultiMachineProductDto dto) {
        // IllegalArgumentException  → 400  (handled by GlobalExceptionHandler)
        // RuntimeException          → 409  (handled by GlobalExceptionHandler via DuplicateResourceException)
        List<ProductMachine> saved = productMachineService.createProductOnMultipleMachines(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("productMachines",  saved);
        response.put("successCount",     saved.size());
        response.put("totalRequested",   dto.getMachineIds().size());
        response.put("message", "Successfully created product on " + saved.size()
                + " out of " + dto.getMachineIds().size() + " machines");

        return ResponseUtil.createSuccessResponse(response);
    }

    @PostMapping("/updateProduct")
    public ResponseEntity<?> updateProduct(@RequestBody UpdateProductDto dto) {
        List<ProductMachine> updated = productMachineService.updateProductOnMultipleMachines(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("productMachines", updated);
        response.put("successCount",    updated.size());

        if (dto.getMachineIds() != null) {
            response.put("totalRequested", dto.getMachineIds().size());
            response.put("message", "Successfully updated/created product on " + updated.size()
                    + " out of " + dto.getMachineIds().size() + " machines");
        } else {
            response.put("totalRequested", 1);
            response.put("message", "Product updated successfully");
        }

        return ResponseUtil.createSuccessResponse(response);
    }

    @PostMapping("/updateProductMachineCompletion")
    public ResponseEntity<?> updateProductMachineCompletion(@RequestBody UpdateProductMachineCompletionDto dto) {
        List<ProductMachine> updated = productMachineService.updateProductMachineCompletion(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("updatedMachines", updated.size());
        response.put("productMachines", updated);
        response.put("message", "Successfully updated " + updated.size() + " machine(s) for product");

        return ResponseUtil.createSuccessResponse(response);
    }

    @GetMapping("/checkMachineAvailability")
    public ResponseEntity<?> checkMachineAvailability(@RequestParam int machineId) {
        Map<String, Object> response = new HashMap<>();
        response.put("available", true);
        response.put("machineId", machineId);
        response.put("message",   "Machine availability check");
        return ResponseUtil.createSuccessResponse(response);
    }
}
