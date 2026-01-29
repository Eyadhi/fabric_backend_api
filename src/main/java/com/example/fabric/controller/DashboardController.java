package com.example.fabric.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.DashboardStatsDto;
import com.example.fabric.model.ApiResponse;
import com.example.fabric.services.DashboardService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    /**
     * Get consolidated dashboard statistics
     * 
     * Endpoint: GET /users/dashboard/stats
     * 
     * Response includes:
     * - Total workers count
     * - Total machines count
     * - Total products count
     * - Total shifts count
     * - Total pieces count
     * 
     * @return DashboardStatsDto with all counts
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<?>> getDashboardStats() {
        try {
            DashboardStatsDto stats = dashboardService.getDashboardStats();
            return ResponseUtil.createSuccessResponse(stats);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error fetching dashboard statistics: " + e.getMessage());
        }
    }
}
