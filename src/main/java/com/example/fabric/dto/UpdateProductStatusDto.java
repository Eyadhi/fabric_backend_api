package com.example.fabric.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductStatusDto {
    private Long productId;
    private int isComplete; // 1 = in progress, 2 = completed
    private LocalDate endDate; // Optional - can be null
}