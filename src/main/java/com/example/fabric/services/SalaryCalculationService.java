package com.example.fabric.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fabric.dto.SalaryCalculationDto;
import com.example.fabric.dto.SalaryCalculationDto.MachineProductionDto;
import com.example.fabric.dto.ShiftAssignmentDto;
import com.example.fabric.model.Meter;
import com.example.fabric.model.Product;
import com.example.fabric.model.WorkerShiftAssignment;
import com.example.fabric.repository.MeterRepository;
import com.example.fabric.repository.ProductRepository;
import com.example.fabric.repository.WorkerRepository;
import com.example.fabric.repository.MachineRepository;
import com.example.fabric.repository.ShiftAssignmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SalaryCalculationService {

    private final MeterRepository meterRepository;
    private final ProductRepository productRepository;
    private final WorkerRepository workerRepository;
    private final MachineRepository machineRepository;
    private final ShiftAssignmentRepository assignmentRepository;

    /**
     * Calculate weekly salary (Saturday to Friday)
     */
    public List<SalaryCalculationDto> calculateWeeklySalary(LocalDate weekEndDate) {
        LocalDate weekStartDate = weekEndDate.minusDays(6); // Saturday to Friday
        return calculateSalaryForPeriod(weekStartDate, weekEndDate, 1);
    }

    /**
     * Calculate salary for custom period
     */
    public List<SalaryCalculationDto> calculateCustomPeriodSalary(LocalDate startDate, LocalDate endDate) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        int weeks = (int) Math.ceil(daysBetween / 7.0);
        return calculateSalaryForPeriod(startDate, endDate, weeks);
    }

    /**
     * Calculate salary for a specific period and weeks
     */
    private List<SalaryCalculationDto> calculateSalaryForPeriod(LocalDate startDate, LocalDate endDate, Integer weeks) {
        List<ShiftAssignmentDto> assignments = getAssignmentsForSalaryCalculation(endDate, weeks);
        
        // Group by worker
        Map<Long, List<ShiftAssignmentDto>> workerAssignments = assignments.stream()
                .collect(Collectors.groupingBy(ShiftAssignmentDto::getWorkerId));

        List<SalaryCalculationDto> salaryCalculations = new ArrayList<>();

        for (Map.Entry<Long, List<ShiftAssignmentDto>> entry : workerAssignments.entrySet()) {
            Long workerId = entry.getKey();
            List<ShiftAssignmentDto> workerAssignmentList = entry.getValue();

            SalaryCalculationDto salaryCalc = calculateWorkerSalary(workerId, workerAssignmentList, startDate, endDate, weeks);
            salaryCalculations.add(salaryCalc);
        }

        // Mark assignments as salary calculated (if needed in future)
        // List<Long> assignmentIds = assignments.stream()
        //         .map(dto -> dto.getWorkerId()) // This should be assignment ID, but we need to modify the DTO
        //         .collect(Collectors.toList());
        
        return salaryCalculations;
    }

    /**
     * Get assignments for salary calculation
     */
    public List<ShiftAssignmentDto> getAssignmentsForSalaryCalculation(LocalDate endDate, Integer weeks) {
        LocalDate startDate = endDate.minusDays((weeks * 7) - 1);
        
        List<WorkerShiftAssignment> assignments = assignmentRepository.findAll()
                .stream()
                .filter(assignment -> assignment.getIsActive())
                .filter(assignment -> !(endDate.isBefore(assignment.getPeriodStartDate()) || 
                                      startDate.isAfter(assignment.getPeriodEndDate())))
                .collect(Collectors.toList());
        
        return assignments.stream()
                .map(assignment -> convertToDto(assignment))
                .collect(Collectors.toList());
    }

    /**
     * Convert WorkerShiftAssignment to DTO
     */
    private ShiftAssignmentDto convertToDto(WorkerShiftAssignment assignment) {
        ShiftAssignmentDto dto = new ShiftAssignmentDto();
        dto.setWorkerId(assignment.getWorker().getId());
        dto.setMachineId(assignment.getMachine().getId());
        dto.setShiftId(assignment.getShift().getId());
        dto.setPeriodStartDate(assignment.getPeriodStartDate());
        dto.setPeriodEndDate(assignment.getPeriodEndDate());
        dto.setWorkerName(assignment.getWorker().getWorkerName());
        dto.setMachineCode(assignment.getMachine().getMachineCode());
        dto.setShiftName(assignment.getShift().getShiftName());
        dto.setIsActive(assignment.getIsActive());
        dto.setAssignmentType(assignment.getAssignmentType());
        return dto;
    }

    /**
     * Calculate salary for a specific worker
     */
    private SalaryCalculationDto calculateWorkerSalary(Long workerId, List<ShiftAssignmentDto> assignments, 
                                                      LocalDate startDate, LocalDate endDate, Integer weeks) {
        
        // Get all meters for this worker in the period
        List<Meter> meters = meterRepository.findByWorkerIdAndProductionDateBetween(workerId, startDate, endDate);
        
        // Group meters by machine and product
        Map<String, Map<Long, BigDecimal>> machineProductMeters = new HashMap<>();
        for (Meter meter : meters) {
            String machineKey = meter.getMachine().getId().toString();
            Long productId = meter.getProduct().getId();
            
            machineProductMeters.computeIfAbsent(machineKey, k -> new HashMap<>())
                    .merge(productId, meter.getMeters(), BigDecimal::add);
        }

        // Calculate totals and create machine production details
        List<MachineProductionDto> machineProductions = new ArrayList<>();
        BigDecimal grandTotalMeters = BigDecimal.ZERO;
        BigDecimal grandTotalAdjustedMeters = BigDecimal.ZERO;
        BigDecimal grandTotalCost = BigDecimal.ZERO;

        for (Map.Entry<String, Map<Long, BigDecimal>> machineEntry : machineProductMeters.entrySet()) {
            Long machineId = Long.valueOf(machineEntry.getKey());
            
            for (Map.Entry<Long, BigDecimal> productEntry : machineEntry.getValue().entrySet()) {
                Long productId = productEntry.getKey();
                BigDecimal totalMeters = productEntry.getValue();
                
                Product product = productRepository.findById(productId).orElse(null);
                
                // Calculate adjusted meters after point decrease
                BigDecimal adjustedMeters = totalMeters;
                BigDecimal pointDecrease = BigDecimal.ZERO;
                
                if (product != null && product.getPointDecrease() != null) {
                    pointDecrease = product.getPointDecrease();
                    BigDecimal decreaseAmount = totalMeters.multiply(pointDecrease)
                            .divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
                    adjustedMeters = totalMeters.subtract(decreaseAmount);
                }
                
                // Round adjusted meters to 1 decimal place
                adjustedMeters = adjustedMeters.setScale(1, RoundingMode.HALF_UP);
                
                // Calculate cost
                BigDecimal totalCost = product != null ? 
                        adjustedMeters.multiply(BigDecimal.valueOf(product.getCostout())) : BigDecimal.ZERO;
                
                // Round total cost to 1 decimal place
                totalCost = totalCost.setScale(1, RoundingMode.HALF_UP);
                
                // Create machine production DTO
                MachineProductionDto machineProduction = new MachineProductionDto();
                machineProduction.setMachineId(machineId);
                machineProduction.setMachineCode(getMachineCode(machineId));
                machineProduction.setProductId(productId);
                machineProduction.setProductName(product != null ? product.getProductName() : "Unknown");
                machineProduction.setMeters(totalMeters.setScale(1, RoundingMode.HALF_UP));
                machineProduction.setAdjustedMeters(adjustedMeters);
                machineProduction.setCost(totalCost);
                machineProduction.setPointDecrease(pointDecrease.setScale(1, RoundingMode.HALF_UP));
                
                machineProductions.add(machineProduction);
                
                // Add to grand totals
                grandTotalMeters = grandTotalMeters.add(totalMeters);
                grandTotalAdjustedMeters = grandTotalAdjustedMeters.add(adjustedMeters);
                grandTotalCost = grandTotalCost.add(totalCost);
            }
        }

        // Create salary calculation DTO
        SalaryCalculationDto salaryCalc = new SalaryCalculationDto();
        salaryCalc.setWorkerId(workerId);
        salaryCalc.setWorkerName(getWorkerName(workerId));
        salaryCalc.setPeriodStartDate(startDate);
        salaryCalc.setPeriodEndDate(endDate);
        salaryCalc.setCalculationWeeks(weeks);
        salaryCalc.setTotalMeters(grandTotalMeters.setScale(1, RoundingMode.HALF_UP));
        salaryCalc.setAdjustedMeters(grandTotalAdjustedMeters.setScale(1, RoundingMode.HALF_UP));
        salaryCalc.setTotalCost(grandTotalCost.setScale(1, RoundingMode.HALF_UP));
        salaryCalc.setMachineProductions(machineProductions);
        salaryCalc.setIsCalculated(true);
        salaryCalc.setCalculatedDate(LocalDate.now());

        return salaryCalc;
    }

    // Helper methods
    private String getMachineCode(Long machineId) {
        return machineRepository.findById(machineId)
                .map(machine -> machine.getMachineCode())
                .orElse("M" + machineId);
    }

    private String getWorkerName(Long workerId) {
        return workerRepository.findById(workerId)
                .map(worker -> worker.getWorkerName())
                .orElse("Worker " + workerId);
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

    public Map<String, Object> getTotalMetersAndCost(Long workerId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return getTotalMetersAndCost(workerId, weekStart, weekEnd);
    }
}