package com.example.fabric.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExpenseResponseDto {
    private Long id;
    private String expensePurpose;
    private int price;
    private Long typeId;
    private String typeName;
    private LocalDate expenseDate;
}
