package com.example.fabric.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.SalaryCalculationDto;
import com.example.fabric.services.SalaryCalculationService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryCalculationService salaryCalculationService;

    /**
     * Calculate weekly salary (Saturday to Friday)
     */
    @PostMapping("/calculate-weekly-salary")
    public ResponseEntity<?> calculateWeeklySalary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEndDate) {
        try {
            List<SalaryCalculationDto> calculations = salaryCalculationService.calculateWeeklySalary(weekEndDate);
            return ResponseUtil.createSuccessResponse(calculations);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Error calculating weekly salary: " + e.getMessage());
        }
    }

    /**
     * Calculate custom period salary
     */
    @PostMapping("/calculate-custom-salary")
    public ResponseEntity<?> calculateCustomPeriodSalary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<SalaryCalculationDto> calculations = 
                    salaryCalculationService.calculateCustomPeriodSalary(startDate, endDate);
            return ResponseUtil.createSuccessResponse(calculations);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Error calculating custom salary: " + e.getMessage());
        }
    }
}
