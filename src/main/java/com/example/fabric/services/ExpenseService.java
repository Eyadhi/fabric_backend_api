package com.example.fabric.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.fabric.dto.AddExpenseDto;
import com.example.fabric.dto.ExpenseResponseDto;
import com.example.fabric.model.Expense;
import com.example.fabric.model.ExpenseType;
import com.example.fabric.repository.ExpenseRepository;
import com.example.fabric.repository.ExpenseTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseTypeRepository expenseTypeRepository;

    public void addExpense(AddExpenseDto dto) {

        ExpenseType type = expenseTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new RuntimeException("Invalid expense type"));

        Expense expense = new Expense();
        expense.setExpensePurpose(dto.getExpensePurpose());
        expense.setPrice(dto.getPrice());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setType(type);

        expenseRepository.save(expense);
    }

    public Map<String, Object> getExpenses(
            Integer year,
            Integer month,
            LocalDate startDate,
            LocalDate endDate) {

        Map<String, Object> response = new HashMap<>();

        if (year == null && month == null && startDate == null && endDate == null) {

            response.put("expenses", expenseRepository.getAllExpenses());
            response.put("totalAmount", expenseRepository.getTotalForAllExpenses());

            return response;
        }

        if (startDate != null && endDate != null) {

            // Optional safety check
            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date must be after start date");
            }

            response.put(
                    "expenses",
                    expenseRepository.getExpensesBetweenDates(startDate, endDate));
            response.put(
                    "totalAmount",
                    expenseRepository.getTotalBetweenDates(startDate, endDate));
            return response;
        }

        if (year != null && month != null) {
            startDate = LocalDate.of(year, month, 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else if (year != null) {
            startDate = LocalDate.of(year, 1, 1);
            endDate = LocalDate.of(year, 12, 31);
        } else {
            startDate = null;
            endDate = null;
        }

        List<ExpenseResponseDto> expenses = expenseRepository.getExpensesBetweenDates(startDate, endDate);

        BigDecimal totalAmount = expenseRepository.getTotalBetweenDates(startDate, endDate);

        response.put("expenses", expenses);
        response.put("totalAmount", totalAmount);

        return response;
    }

}
