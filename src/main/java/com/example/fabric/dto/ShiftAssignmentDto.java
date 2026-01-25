package com.example.fabric.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ShiftAssignmentDto {
    private Long workerId;
    private Long machineId;
    private Long shiftId;
    
    // For both types
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    
    // Response fields
    private String workerName;
    private String machineCode;
    private String shiftName;
    private Boolean isActive;
    
    private int assignmentType;
}