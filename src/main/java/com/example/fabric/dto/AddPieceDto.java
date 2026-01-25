package com.example.fabric.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddPieceDto {
    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "exportDate is required")
    private LocalDate exportDate;

    @NotNull(message = "meters is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "meters must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "meters must have up to 10 digits and 2 decimals")
    private BigDecimal meters;
}
