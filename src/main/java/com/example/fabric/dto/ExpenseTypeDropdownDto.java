package com.example.fabric.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExpenseTypeDropdownDto {
    private Long id;
    private String typeName;
}
