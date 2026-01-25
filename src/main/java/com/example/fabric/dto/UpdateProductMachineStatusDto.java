package com.example.fabric.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UpdateProductMachineStatusDto {
    private Long productMachineId;
    private Integer isComplete;
    private LocalDate endDate;
}