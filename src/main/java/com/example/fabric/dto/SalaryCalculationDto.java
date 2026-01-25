package com.example.fabric.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class SalaryCalculationDto {
    private Long workerId;
    private String workerName;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private Integer calculationWeeks;
    
    // Meter and salary details
    private BigDecimal totalMeters;
    private BigDecimal totalCost;
    private BigDecimal adjustedMeters; // After point decrease
    
    // Machine-wise breakdown
    private List<MachineProductionDto> machineProductions;
    
    // Calculation status
    private Boolean isCalculated;
    private LocalDate calculatedDate;
    
    @Data
    public static class MachineProductionDto {
        private Long machineId;
        private String machineCode;
        private Long productId;
        private String productName;
        private BigDecimal meters;
        private BigDecimal adjustedMeters;
        private BigDecimal cost;
        private BigDecimal pointDecrease;
    }
}