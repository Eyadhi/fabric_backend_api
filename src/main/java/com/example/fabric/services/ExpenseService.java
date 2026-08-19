package com.example.fabric.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.fabric.dto.AddExpenseDto;
import com.example.fabric.dto.ExpenseResponseDto;
import com.example.fabric.exceptions.BadRequestException;
import com.example.fabric.exceptions.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Expense type not found with id: " + dto.getTypeId()));

        Expense expense = new Expense();
        expense.setExpensePurpose(dto.getExpensePurpose());
        expense.setPrice(dto.getPrice());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setType(type);

        expenseRepository.save(expense);
    }

    public Map<String, Object> getExpenses(Integer year, Integer month,
            LocalDate startDate, LocalDate endDate) {

        Map<String, Object> response = new HashMap<>();

        // No filters — return all
        if (year == null && month == null && startDate == null && endDate == null) {
            response.put("expenses",     expenseRepository.getAllExpenses());
            response.put("totalAmount",  expenseRepository.getTotalForAllExpenses());
            return response;
        }

        // Explicit date range provided
        if (startDate != null && endDate != null) {
            if (endDate.isBefore(startDate)) {
                throw new BadRequestException("endDate must not be before startDate");
            }
            response.put("expenses",    expenseRepository.getExpensesBetweenDates(startDate, endDate));
            response.put("totalAmount", expenseRepository.getTotalBetweenDates(startDate, endDate));
            return response;
        }

        // Derive date range from year / month
        LocalDate from;
        LocalDate to;
        if (year != null && month != null) {
            from = LocalDate.of(year, month, 1);
            to   = from.withDayOfMonth(from.lengthOfMonth());
        } else if (year != null) {
            from = LocalDate.of(year, 1, 1);
            to   = LocalDate.of(year, 12, 31);
        } else {
            // month only — use current year
            from = LocalDate.of(LocalDate.now().getYear(), month, 1);
            to   = from.withDayOfMonth(from.lengthOfMonth());
        }

        List<ExpenseResponseDto> expenses = expenseRepository.getExpensesBetweenDates(from, to);
        BigDecimal totalAmount = expenseRepository.getTotalBetweenDates(from, to);

        response.put("expenses",    expenses);
        response.put("totalAmount", totalAmount);
        return response;
    }
}
