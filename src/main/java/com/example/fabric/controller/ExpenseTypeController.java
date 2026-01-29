package com.example.fabric.controller;

import com.example.fabric.dto.AddExpenseTypeDto;
import com.example.fabric.services.ExpenseTypeService;
import com.example.fabric.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ExpenseTypeController {

    private final ExpenseTypeService expenseTypeService;

    @GetMapping("/expenseType")
    public ResponseEntity<?> getExpenseTypeDropdown() {
        return ResponseUtil.createSuccessResponse(
                expenseTypeService.getExpenseTypeDropdown());
    }

    @PostMapping("/addExpenseType")
    public ResponseEntity<?> addExpenseType(
            @Valid @RequestBody AddExpenseTypeDto dto) {

        try {
            return ResponseUtil.createSuccessResponse(
                    expenseTypeService.addExpenseType(dto));
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    500,
                    "Failed to add expense type: " + e.getMessage());
        }
    }

}