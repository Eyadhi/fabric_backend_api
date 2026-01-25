package com.example.fabric.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithMachinesDto {
    private Long id;
    private String productCode;
    private String productName;
    private String description;
    private int meters;
    private int costin;
    private int costout;
    private double pointDecrease;
    private BigDecimal metersout;
    
    // Overall product dates and status
    private LocalDate startDate;
    private LocalDate endDate;
    private int isComplete;
    
    // Machine information
    private List<MachineInfo> machines;
    
    // For backward compatibility with frontend expecting machineId
    private Long machineId; // Will be set to first machine ID for compatibility
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MachineInfo {
        private Long machineId;
        private String machineCode;
        private String machineName;
        private LocalDate startDate;
        private LocalDate endDate;
        private int isComplete;
    }
}