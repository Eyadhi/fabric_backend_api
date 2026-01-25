package com.example.fabric.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AddProductDto {
    private String productCode;
    private String productName;
    private String description;
    private int meters;
    private int costin;
    private int costout;
    private int machineId;
    private LocalDate startDate;
    private Double pointDecrease;
}
