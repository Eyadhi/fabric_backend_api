package com.example.fabric.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductMachineCompletionDto {
    private Long productId;
    private List<Long> machineIds; // Machines to mark as completed
    private LocalDate endDate; // Optional end date for completion
    private int isComplete; // 1 = reopen, 2 = complete
}