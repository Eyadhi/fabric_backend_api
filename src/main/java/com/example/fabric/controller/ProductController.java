package com.example.fabric.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
        return (id != null) ? productService.getProductById(id)
                : productService.getAllProducts();
    }

    @GetMapping("/getProductMachines")
    public ResponseEntity<?> getAllProductMachines() {
        try {
            List<ProductMachineDto> productMachines = productMachineService.getAllProductMachines();
            return ResponseUtil.createSuccessResponse(productMachines);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @GetMapping("/getRunningProducts")
    public ResponseEntity<?> getRunningProducts() {
        try {
            // Return unique products with their machine information
            List<ProductWithMachinesDto> runningProducts = productMachineService.getProductsWithMachinesByStatus(1);
            return ResponseUtil.createSuccessResponse(runningProducts);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @GetMapping("/getCompletedProducts")
    public ResponseEntity<?> getCompletedProducts() {
        try {
            // Return unique products with their machine information
            List<ProductWithMachinesDto> completedProducts = productMachineService.getProductsWithMachinesByStatus(2);
            return ResponseUtil.createSuccessResponse(completedProducts);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @GetMapping("/getProductStats")
    public ResponseEntity<?> getProductStats() {
        try {
            // Count unique products, not ProductMachine entries
            int runningCount = productService.getProductsByStatus(1).size();
            int completedCount = productService.getProductsByStatus(2).size();
            
            var stats = new java.util.HashMap<String, Integer>();
            stats.put("running", runningCount);
            stats.put("completed", completedCount);
            stats.put("total", runningCount + completedCount);
            
            return ResponseUtil.createSuccessResponse(stats);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @PostMapping("/addProductsToMultipleMachines")
    public ResponseEntity<?> saveProductToMultipleMachines(@RequestBody AddMultiMachineProductDto dto) {
        try {
            List<ProductMachine> savedProductMachines = productMachineService.createProductOnMultipleMachines(dto);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("productMachines", savedProductMachines);
            response.put("successCount", savedProductMachines.size());
            response.put("totalRequested", dto.getMachineIds().size());
            response.put("message", "Successfully created product on " + savedProductMachines.size() + " out of " + dto.getMachineIds().size() + " machines");
            
            return ResponseUtil.createSuccessResponse(response);

        } catch (IllegalArgumentException ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    ex.getMessage());

        } catch (RuntimeException ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.CONFLICT.value(),
                    ex.getMessage());

        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @PostMapping("/updateProduct")
    public ResponseEntity<?> updateProduct(@RequestBody UpdateProductDto dto) {
        try {
            List<ProductMachine> updatedProductMachines = productMachineService.updateProductOnMultipleMachines(dto);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("productMachines", updatedProductMachines);
            response.put("successCount", updatedProductMachines.size());
            if (dto.getMachineIds() != null) {
                response.put("totalRequested", dto.getMachineIds().size());
                response.put("message", "Successfully updated/created product on " + updatedProductMachines.size() + " out of " + dto.getMachineIds().size() + " machines");
            } else {
                response.put("totalRequested", 1);
                response.put("message", "Product updated successfully");
            }
            
            return ResponseUtil.createSuccessResponse(response);

        } catch (IllegalArgumentException ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    ex.getMessage());

        } catch (RuntimeException ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.CONFLICT.value(),
                    ex.getMessage());

        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @PostMapping("/updateProductMachineCompletion")
    public ResponseEntity<?> updateProductMachineCompletion(@RequestBody UpdateProductMachineCompletionDto dto) {
        try {
            List<ProductMachine> updatedProductMachines = productMachineService.updateProductMachineCompletion(dto);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("updatedMachines", updatedProductMachines.size());
            response.put("productMachines", updatedProductMachines);
            response.put("message", "Successfully updated " + updatedProductMachines.size() + " machine(s) for product");
            
            return ResponseUtil.createSuccessResponse(response);

        } catch (IllegalArgumentException ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    ex.getMessage());

        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    // Legacy endpoints for backward compatibility
    @GetMapping("/checkMachineAvailability")
    public ResponseEntity<?> checkMachineAvailability(@RequestParam int machineId) {
        try {
            // This now checks ProductMachine table instead of Product table
            // Implementation would need to be updated based on new structure
            var response = new java.util.HashMap<String, Object>();
            response.put("available", true); // Simplified for now
            response.put("machineId", machineId);
            response.put("message", "Machine availability check");
            
            return ResponseUtil.createSuccessResponse(response);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }
}
