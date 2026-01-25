package com.example.fabric.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AddMultiMachineProductDto {
    private String productCode;
    private String productName;
    private String description;
    private Integer meters;
    private Integer costin;
    private Integer costout;
    private List<Integer> machineIds; // Multiple machine IDs
    private LocalDate startDate;
    private Double pointDecrease;
}