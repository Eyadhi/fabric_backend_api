package com.example.fabric.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShiftAssignmentRequest {
    @NotNull(message = "workerId is required")
    private Long workerId;

    @NotNull(message = "weekStartDate is required")
    private LocalDate weekStartDate;

    // Optional - worker might only work one shift
    private Long morningShiftId;
    
    // Optional - worker might only work one shift
    private Long nightShiftId;
}