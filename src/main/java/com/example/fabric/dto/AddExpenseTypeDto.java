package com.example.fabric.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddExpenseTypeDto {

    @NotBlank(message = "Expense type name is required")
    private String typeName;
}
