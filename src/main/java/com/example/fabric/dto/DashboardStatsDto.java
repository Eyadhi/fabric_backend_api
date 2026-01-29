package com.example.fabric.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard Statistics DTO
 * Contains all dashboard metrics in a single response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalWorkers;
    private long totalMachines;
    private long totalProducts;
    private long totalShifts;
    private long totalPieces;
}
