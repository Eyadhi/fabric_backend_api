package com.example.fabric.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ProductMachineDto {
    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String description;
    private Integer meters;
    private Integer costin;
    private Integer costout;
    private Double pointDecrease;
    private Long machineId;
    private String machineCode;
    private String machineName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer isComplete;
}