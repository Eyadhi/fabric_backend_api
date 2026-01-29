package com.example.fabric.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.AddExpenseDto;
import com.example.fabric.services.ExpenseService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/addExpenses")
    public ResponseEntity<?> addExpense(@RequestBody AddExpenseDto dto) {
        try {
            expenseService.addExpense(dto);
            return ResponseUtil.createSuccessResponse("Expense added successfully");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    500,
                    "Failed to add expense: " + e.getMessage());
        }
    }

    @GetMapping("/getExpenses")
    public ResponseEntity<?> getExpenses(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            return ResponseUtil.createSuccessResponse(
                    expenseService.getExpenses(year, month, startDate, endDate));
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    500,
                    "Failed to fetch expenses: " + e.getMessage());
        }
    }

}
