package com.example.fabric.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AddExpenseDto {
    private String expensePurpose;
    private int price;
    private Long typeId;
    private LocalDate expenseDate;
}