package com.example.fabric.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class UpdateProductDto {
    private Long id;
    private String productCode;
    private String productName;
    private String description;
    private Integer meters;
    private Integer costin;
    private Integer costout;
    private List<Integer> machineIds; // Support multiple machines
    private LocalDate startDate;
    private Double pointDecrease;
}