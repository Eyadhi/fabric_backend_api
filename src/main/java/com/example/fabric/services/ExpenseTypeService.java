package com.example.fabric.services;

import com.example.fabric.dto.AddExpenseTypeDto;
import com.example.fabric.dto.ExpenseTypeDropdownDto;
import com.example.fabric.exceptions.DuplicateResourceException;
import com.example.fabric.model.ExpenseType;
import com.example.fabric.repository.ExpenseTypeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseTypeService {

    private final ExpenseTypeRepository expenseTypeRepository;

    public List<ExpenseTypeDropdownDto> getExpenseTypeDropdown() {
        return expenseTypeRepository.getExpenseTypeDropdown();
    }

    public ExpenseType addExpenseType(AddExpenseTypeDto dto) {
        expenseTypeRepository.findByTypeIgnoreCase(dto.getTypeName())
                .ifPresent(e -> {
                    throw new DuplicateResourceException("Expense type already exists: " + dto.getTypeName());
                });

        ExpenseType expenseType = new ExpenseType();
        expenseType.setType(dto.getTypeName());
        return expenseTypeRepository.save(expenseType);
    }
}
