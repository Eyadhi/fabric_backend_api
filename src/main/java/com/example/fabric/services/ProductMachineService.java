package com.example.fabric.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.fabric.dto.AddMultiMachineProductDto;
import com.example.fabric.dto.ProductMachineDto;
import com.example.fabric.dto.ProductWithMachinesDto;
import com.example.fabric.dto.UpdateProductDto;
import com.example.fabric.dto.UpdateProductMachineCompletionDto;
import com.example.fabric.dto.UpdateProductMachineStatusDto;
import com.example.fabric.exceptions.BusinessException;
import com.example.fabric.exceptions.ResourceNotFoundException;
import com.example.fabric.model.Machine;
import com.example.fabric.model.Product;
import com.example.fabric.model.ProductMachine;
import com.example.fabric.repository.MachineRepository;
import com.example.fabric.repository.ProductMachineRepository;
import com.example.fabric.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductMachineService {

    private static final Logger log = LoggerFactory.getLogger(ProductMachineService.class);

    private final ProductRepository productRepository;
    private final ProductMachineRepository productMachineRepository;
    private final MachineRepository machineRepository;

    public List<ProductMachine> createProductOnMultipleMachines(AddMultiMachineProductDto dto) {
        List<ProductMachine> createdProductMachines = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // First, create or find the product
        Product product = new Product();
        product.setProductCode(dto.getProductCode());
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setMeters(dto.getMeters());
        product.setStartDate(dto.getStartDate());
        product.setCostin(dto.getCostin());
        product.setCostout(dto.getCostout());
        product.setPointDecrease(dto.getPointDecrease() != null ? 
            BigDecimal.valueOf(dto.getPointDecrease()) : BigDecimal.ZERO);

        Product savedProduct = productRepository.save(product);

        // Create ProductMachine entries for each selected machine
        for (Integer machineId : dto.getMachineIds()) {
            try {
                // Check if there's already a running product on this machine
                ProductMachine runningProductOnMachine = productMachineRepository.findByMachineIdAndIsComplete(machineId.longValue(), 1);
                if (runningProductOnMachine != null) {
                    errors.add("Machine " + machineId + " already has running product: " + runningProductOnMachine.getProduct().getProductName());
                    continue;
                }

                Machine machine = machineRepository.findById(machineId.longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Machine", machineId.longValue()));

                ProductMachine productMachine = new ProductMachine();
                productMachine.setProduct(savedProduct);
                productMachine.setMachine(machine);
                productMachine.setStartDate(dto.getStartDate());
                productMachine.setIsComplete(1); // Start as running

                ProductMachine savedProductMachine = productMachineRepository.save(productMachine);
                createdProductMachines.add(savedProductMachine);
            } catch (Exception e) {
                errors.add("Failed to create product on machine " + machineId + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty() && createdProductMachines.isEmpty()) {
            throw new BusinessException(
                    "Failed to create product on any machine: " + String.join(", ", errors));
        } else if (!errors.isEmpty()) {
            log.warn("Partial success creating product on machines — failed: {}", String.join(", ", errors));
        }

        // Update the overall Product status after creating ProductMachine entries
        if (!createdProductMachines.isEmpty()) {
            updateProductOverallStatus(savedProduct.getId());
        }

        return createdProductMachines;
    }

    public List<ProductMachine> updateProductOnMultipleMachines(UpdateProductDto dto) {
        // This is more complex - we need to update the product and manage machine assignments
        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", dto.getId()));

        // Update product details
        if (dto.getProductCode() != null) {
            product.setProductCode(dto.getProductCode());
        }
        if (dto.getProductName() != null) {
            product.setProductName(dto.getProductName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getMeters() != null) {
            product.setMeters(dto.getMeters());
        }
        if (dto.getCostin() != null) {
            product.setCostin(dto.getCostin());
        }
        if (dto.getCostout() != null) {
            product.setCostout(dto.getCostout());
        }
        if (dto.getPointDecrease() != null) {
            product.setPointDecrease(BigDecimal.valueOf(dto.getPointDecrease()));
        }

        productRepository.save(product);

        // Handle machine assignments
        List<ProductMachine> result = new ArrayList<>();
        if (dto.getMachineIds() != null && !dto.getMachineIds().isEmpty()) {
            for (Integer machineId : dto.getMachineIds()) {
                // Check if this product-machine combination already exists
                List<ProductMachine> existingPMs = productMachineRepository.findByProductId(product.getId());
                ProductMachine existingPM = existingPMs.stream()
                    .filter(pm -> pm.getMachine().getId().equals(machineId.longValue()))
                    .findFirst()
                    .orElse(null);

                if (existingPM != null) {
                    // Update existing ProductMachine
                    if (dto.getStartDate() != null) {
                        existingPM.setStartDate(dto.getStartDate());
                    }
                    result.add(productMachineRepository.save(existingPM));
                } else {
                    // Create new ProductMachine
                    Machine machine = machineRepository.findById(machineId.longValue())
                        .orElseThrow(() -> new ResourceNotFoundException("Machine", machineId.longValue()));

                    ProductMachine newPM = new ProductMachine();
                    newPM.setProduct(product);
                    newPM.setMachine(machine);
                    newPM.setStartDate(dto.getStartDate() != null ? dto.getStartDate() : LocalDate.now());
                    newPM.setIsComplete(1);

                    result.add(productMachineRepository.save(newPM));
                }
            }
        }

        return result;
    }

    public List<ProductMachineDto> getAllProductMachines() {
        List<ProductMachine> productMachines = productMachineRepository.findAllWithDetails();
        return convertToDto(productMachines);
    }

    public List<ProductMachineDto> getProductMachinesByStatus(int isComplete) {
        List<ProductMachine> productMachines = productMachineRepository.findByIsCompleteWithDetails(isComplete);
        return convertToDto(productMachines);
    }

    public ProductMachine updateProductMachineStatus(UpdateProductMachineStatusDto dto) {
        ProductMachine productMachine = productMachineRepository.findById(dto.getProductMachineId())
                .orElseThrow(() -> new RuntimeException("ProductMachine not found with id: " + dto.getProductMachineId()));

        productMachine.setIsComplete(dto.getIsComplete());
        
        if (dto.getEndDate() != null) {
            productMachine.setEndDate(dto.getEndDate());
        } else if (dto.getIsComplete() == 2) {
            productMachine.setEndDate(LocalDate.now());
        } else if (dto.getIsComplete() == 1) {
            productMachine.setEndDate(null);
        }

        ProductMachine savedProductMachine = productMachineRepository.save(productMachine);
        
        // Update the overall Product status based on all ProductMachine entries
        updateProductOverallStatus(productMachine.getProduct().getId());
        
        return savedProductMachine;
    }

    /**
     * Update completion status for specific machines of a product
     * This will update individual ProductMachine entries and then update the overall Product status
     */
    public List<ProductMachine> updateProductMachineCompletion(UpdateProductMachineCompletionDto dto) {
        List<ProductMachine> updatedProductMachines = new ArrayList<>();
        
        for (Long machineId : dto.getMachineIds()) {
            // Find the ProductMachine entry for this product and machine
            ProductMachine productMachine = productMachineRepository.findByProductIdAndMachineIdAndIsComplete(
                dto.getProductId(), machineId, dto.getIsComplete() == 2 ? 1 : 2); // Find opposite status
            
            if (productMachine == null) {
                // Try to find any ProductMachine for this product and machine regardless of status
                List<ProductMachine> allPMs = productMachineRepository.findByProductId(dto.getProductId());
                productMachine = allPMs.stream()
                    .filter(pm -> pm.getMachine().getId().equals(machineId))
                    .findFirst()
                    .orElse(null);
            }
            
            if (productMachine != null) {                productMachine.setIsComplete(dto.getIsComplete());
                
                if (dto.getEndDate() != null) {
                    productMachine.setEndDate(dto.getEndDate());
                } else if (dto.getIsComplete() == 2) {
                    productMachine.setEndDate(LocalDate.now());
                } else if (dto.getIsComplete() == 1) {
                    productMachine.setEndDate(null);
                }
                
                ProductMachine savedPM = productMachineRepository.save(productMachine);
                updatedProductMachines.add(savedPM);
            } else {
                throw new RuntimeException("ProductMachine not found for product " + dto.getProductId() + " and machine " + machineId);
            }
        }
        
        // Update the overall Product status based on all ProductMachine entries
        updateProductOverallStatus(dto.getProductId());
        
        return updatedProductMachines;
    }
    private void updateProductOverallStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        List<ProductMachine> productMachines = productMachineRepository.findByProductId(productId);
        
        if (productMachines.isEmpty()) {
            return; // No machine assignments
        }
        
        // Find earliest start date
        LocalDate earliestStartDate = productMachines.stream()
                .map(ProductMachine::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(product.getStartDate());
        
        // Check if all machines are completed
        boolean allCompleted = productMachines.stream()
                .allMatch(pm -> pm.getIsComplete() == 2);
        
        // Find latest end date if all completed
        LocalDate latestEndDate = null;
        if (allCompleted) {
            latestEndDate = productMachines.stream()
                    .map(ProductMachine::getEndDate)
                    .filter(date -> date != null)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());
        }
        
        // Update product
        product.setStartDate(earliestStartDate);
        product.setEndDate(latestEndDate);
        product.setIsComplete(allCompleted ? 2 : 1);
        
        productRepository.save(product);
    }

    private List<ProductMachineDto> convertToDto(List<ProductMachine> productMachines) {
        return productMachines.stream().map(pm -> {
            ProductMachineDto dto = new ProductMachineDto();
            dto.setId(pm.getId());
            dto.setProductId(pm.getProduct().getId());
            dto.setProductCode(pm.getProduct().getProductCode());
            dto.setProductName(pm.getProduct().getProductName());
            dto.setDescription(pm.getProduct().getDescription());
            dto.setMeters(pm.getProduct().getMeters());
            dto.setCostin(pm.getProduct().getCostin());
            dto.setCostout(pm.getProduct().getCostout());
            dto.setPointDecrease(pm.getProduct().getPointDecrease() != null ? 
                pm.getProduct().getPointDecrease().doubleValue() : 0.0);
            dto.setMachineId(pm.getMachine().getId());
            dto.setMachineCode(pm.getMachine().getMachineCode());
            dto.setMachineName(pm.getMachine().getMachine());
            dto.setStartDate(pm.getStartDate());
            dto.setEndDate(pm.getEndDate());
            dto.setIsComplete(pm.getIsComplete());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Get unique products with their machine information by status
     * This method returns each product only once, even if it's running on multiple machines
     */
    public List<ProductWithMachinesDto> getProductsWithMachinesByStatus(int isComplete) {
        // Get all ProductMachine entries with the specified status
        List<ProductMachine> productMachines = productMachineRepository.findByIsCompleteWithDetails(isComplete);
        
        // Group by product ID
        Map<Long, List<ProductMachine>> productGroups = productMachines.stream()
                .collect(Collectors.groupingBy(pm -> pm.getProduct().getId()));
        
        // Convert to ProductWithMachinesDto
        return productGroups.entrySet().stream().map(entry -> {
            List<ProductMachine> pms = entry.getValue();
            
            // Get the product (all ProductMachine entries have the same product)
            Product product = pms.get(0).getProduct();
            
            ProductWithMachinesDto dto = new ProductWithMachinesDto();
            dto.setId(product.getId());
            dto.setProductCode(product.getProductCode());
            dto.setProductName(product.getProductName());
            dto.setDescription(product.getDescription());
            dto.setMeters(product.getMeters());
            dto.setCostin(product.getCostin());
            dto.setCostout(product.getCostout());
            dto.setPointDecrease(product.getPointDecrease() != null ? 
                product.getPointDecrease().doubleValue() : 0.0);
            dto.setMetersout(product.getMetersout());
            dto.setStartDate(product.getStartDate());
            dto.setEndDate(product.getEndDate());
            dto.setIsComplete(product.getIsComplete());
            
            // Set machineId for backward compatibility (use first machine)
            dto.setMachineId(pms.get(0).getMachine().getId());
            
            // Set machine information
            List<ProductWithMachinesDto.MachineInfo> machineInfos = pms.stream().map(pm -> {
                ProductWithMachinesDto.MachineInfo machineInfo = new ProductWithMachinesDto.MachineInfo();
                machineInfo.setMachineId(pm.getMachine().getId());
                machineInfo.setMachineCode(pm.getMachine().getMachineCode());
                machineInfo.setMachineName(pm.getMachine().getMachine());
                machineInfo.setStartDate(pm.getStartDate());
                machineInfo.setEndDate(pm.getEndDate());
                machineInfo.setIsComplete(pm.getIsComplete());
                return machineInfo;
            }).collect(Collectors.toList());
            
            dto.setMachines(machineInfos);
            
            return dto;
        }).collect(Collectors.toList());
    }
}