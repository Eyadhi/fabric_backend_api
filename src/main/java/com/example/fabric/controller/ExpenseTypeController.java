package com.example.fabric.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.dto.AddExpenseTypeDto;
import com.example.fabric.services.ExpenseTypeService;
import com.example.fabric.util.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ExpenseTypeController {

    private final ExpenseTypeService expenseTypeService;

    @GetMapping("/expenseType")
    public ResponseEntity<?> getExpenseTypeDropdown() {
        return ResponseUtil.createSuccessResponse(expenseTypeService.getExpenseTypeDropdown());
    }

    @PostMapping("/addExpenseType")
    public ResponseEntity<?> addExpenseType(@Valid @RequestBody AddExpenseTypeDto dto) {
        return ResponseUtil.createSuccessResponse(expenseTypeService.addExpenseType(dto));
    }
}
