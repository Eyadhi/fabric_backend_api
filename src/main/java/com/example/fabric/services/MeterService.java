package com.example.fabric.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.fabric.dto.MeterRequest;
import com.example.fabric.exceptions.BusinessException;
import com.example.fabric.exceptions.ResourceNotFoundException;
import com.example.fabric.model.Machine;
import com.example.fabric.model.Meter;
import com.example.fabric.model.Product;
import com.example.fabric.model.ProductMachine;
import com.example.fabric.model.Worker;
import com.example.fabric.model.WorkerShiftAssignment;
import com.example.fabric.repository.MachineRepository;
import com.example.fabric.repository.MeterRepository;
import com.example.fabric.repository.ProductMachineRepository;
import com.example.fabric.repository.ProductRepository;
import com.example.fabric.repository.ShiftAssignmentRepository;
import com.example.fabric.repository.WorkerRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final WorkerRepository workerRepository;
    private final MachineRepository machineRepository;
    private final ProductRepository productRepository;
    private final ProductMachineRepository productMachineRepository;
    private final ShiftAssignmentRepository assignmentRepository;

    @Transactional
    public Meter saveMeterProduction(MeterRequest request) {

        Worker worker = workerRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker", request.getWorkerId()));

        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine", request.getMachineId()));

        Product product;
        if (request.getProductId() != null) {
            product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

            ProductMachine productMachine = productMachineRepository.findByProductIdAndMachineIdAndIsComplete(
                    request.getProductId(), request.getMachineId(), 1);
            if (productMachine == null) {
                throw new BusinessException("Product " + request.getProductId()
                        + " is not currently running on machine " + request.getMachineId());
            }
        } else {
            ProductMachine runningProductMachine = productMachineRepository
                    .findByMachineIdAndIsComplete(request.getMachineId(), 1);
            if (runningProductMachine == null) {
                throw new BusinessException("No running product found for machine " + request.getMachineId()
                        + ". Please specify a productId.");
            }
            product = runningProductMachine.getProduct();
        }

        // Automatically determine shift based on worker assignment and production date
        Long shiftId = determineShiftId(
            request.getWorkerId(), 
            request.getMachineId(),
            request.getProductionDate()
        );

        Meter production = new Meter();
        production.setWorker(worker);
        production.setMachine(machine);
        production.setShiftId(shiftId); // Automatically determined
        production.setProduct(product);
        production.setProductionDate(request.getProductionDate());
        production.setMeters(request.getMeters().setScale(1, RoundingMode.HALF_UP));
        return meterRepository.save(production);
    }

    /**
     * Determine shift ID based on worker assignment and production date
     * Uses the new weekly shift assignment system
     */
    private Long determineShiftId(Long workerId, Long machineId, LocalDate productionDate) {
        try {
            // Get worker assignments for the production date
            List<WorkerShiftAssignment> assignments = assignmentRepository.findByWorkerIdAndIsActive(workerId, true);
            
            // Filter for assignments that cover the production date and this specific machine
            java.util.Optional<WorkerShiftAssignment> machineAssignment = assignments.stream()
                .filter(assignment -> assignment.getMachine().getId().equals(machineId))
                .filter(assignment -> !productionDate.isBefore(assignment.getPeriodStartDate()) && 
                                    !productionDate.isAfter(assignment.getPeriodEndDate()))
                .findFirst();
            
            if (machineAssignment.isPresent()) {
                WorkerShiftAssignment assignment = machineAssignment.get();
                return assignment.getShift().getId();
            } else {
                // No specific assignment found, use default shift ID (Morning)
                return 1L; // Default to shift ID 1 (Morning)
            }
        } catch (Exception e) {
            // If there's any error determining the shift, use default
            System.out.println("Warning: Could not determine shift for worker " + workerId + 
                             " on machine " + machineId + " for date " + productionDate + 
                             ". Using default shift. Error: " + e.getMessage());
            return 1L; // Default to shift ID 1 (Morning)
        }
    }

    public List<Meter> getMetersByMachineId(Long machineId) {
        return meterRepository.findByMachineId(machineId);
    }

    public List<Meter> getMetersByWorkerId(Long workerId) {
        return meterRepository.findByWorkerId(workerId);
    }

    public Map<String, Object> getTotalMetersAndCost(Long workerId, LocalDate startDate, LocalDate endDate) {
        List<Meter> meters = meterRepository.findByWorkerIdAndProductionDateBetween(workerId, startDate, endDate);
        
        Map<Long, Map<Long, BigDecimal>> machineProductMeters = new HashMap<>();
        for (Meter meter : meters) {
            Long machineId = meter.getMachine().getId();
            Long productId = meter.getProduct().getId();
            machineProductMeters.computeIfAbsent(machineId, k -> new HashMap<>())
                    .merge(productId, meter.getMeters(), BigDecimal::add);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        BigDecimal grandTotalMeters = BigDecimal.ZERO;
        BigDecimal grandTotalAdjustedMeters = BigDecimal.ZERO;
        BigDecimal grandTotalCost = BigDecimal.ZERO;
        
        for (Map.Entry<Long, Map<Long, BigDecimal>> machineEntry : machineProductMeters.entrySet()) {
            Long machineId = machineEntry.getKey();
            for (Map.Entry<Long, BigDecimal> productEntry : machineEntry.getValue().entrySet()) {
                Long productId = productEntry.getKey();
                BigDecimal totalMeters = productEntry.getValue();
                Product product = productRepository.findById(productId).orElse(null);
                
                // Calculate adjusted meters after applying point decrease
                BigDecimal adjustedMeters = totalMeters;
                BigDecimal pointDecrease = BigDecimal.ZERO;
                
                if (product != null && product.getPointDecrease() != null) {
                    pointDecrease = product.getPointDecrease();
                    // Calculate decrease amount: totalMeters * (pointDecrease / 100)
                    BigDecimal decreaseAmount = totalMeters.multiply(pointDecrease).divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
                    adjustedMeters = totalMeters.subtract(decreaseAmount);
                }
                
                // Calculate cost based on adjusted meters
                BigDecimal totalCost = product != null ? adjustedMeters.multiply(BigDecimal.valueOf(product.getCostout()))
                        : BigDecimal.ZERO;
                
                grandTotalMeters = grandTotalMeters.add(totalMeters);
                grandTotalAdjustedMeters = grandTotalAdjustedMeters.add(adjustedMeters);
                grandTotalCost = grandTotalCost.add(totalCost);
                
                Map<String, Object> item = new HashMap<>();
                item.put("machine_id", machineId.toString());
                item.put("product_id", productId.toString());
                item.put("product_name", product != null ? product.getProductName() : "Unknown");
                item.put("totalmeters", adjustedMeters.setScale(1, RoundingMode.HALF_UP));
                item.put("totalcost", totalCost.setScale(1, RoundingMode.HALF_UP));
                result.add(item);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", result);
        response.put("grandTotalMeters", grandTotalAdjustedMeters.setScale(1, RoundingMode.HALF_UP));
        response.put("grandTotalCost", grandTotalCost.setScale(1, RoundingMode.HALF_UP));
        return response;
    }

    public List<Meter> getMachineWeeklyProduction(Long machineId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return meterRepository.findByMachineIdAndProductionDateBetween(machineId, weekStart, weekEnd);
    }

    public List<Meter> getMachineProductionByDateRange(Long machineId, LocalDate startDate, LocalDate endDate) {
        return meterRepository.findByMachineIdAndProductionDateBetween(machineId, startDate, endDate);
    }
}
