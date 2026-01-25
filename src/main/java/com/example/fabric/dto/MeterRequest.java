package com.example.fabric.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MeterRequest {
    @NotNull(message = "workerId is required")
    private Long workerId;

    @NotNull(message = "machineId is required")
    private Long machineId;

    // shiftId is automatically determined based on:
    // - Worker's weekly shift assignment
    // - Production date (odd days = morning shift worker, even days = night shift worker)

    // productId is optional - if not provided, will use machine's running product
    private Long productId;

    @NotNull(message = "productionDate is required")
    private LocalDate productionDate;

    @NotNull(message = "meters is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "meters must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "meters must have up to 10 digits and 2 decimals")
    private BigDecimal meters;
}
