package com.example.fabric.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.fabric.dto.ExpenseResponseDto;
import com.example.fabric.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("""
                SELECT new com.example.fabric.dto.ExpenseResponseDto(
                    e.id,
                    e.expensePurpose,
                    e.price,
                    t.id,
                    t.type,
                    e.expenseDate
                )
                FROM Expense e
                JOIN e.type t
                WHERE e.status = true
                ORDER BY e.expenseDate DESC
            """)
    List<ExpenseResponseDto> getAllExpenses();

    @Query("""
                SELECT COALESCE(SUM(e.price), 0)
                FROM Expense e
                WHERE e.status = true
            """)
    BigDecimal getTotalForAllExpenses();

    @Query("""
                SELECT new com.example.fabric.dto.ExpenseResponseDto(
                    e.id,
                    e.expensePurpose,
                    e.price,
                    t.id,
                    t.type,
                    e.expenseDate
                )
                FROM Expense e
                JOIN e.type t
                WHERE e.status = true
                AND e.expenseDate BETWEEN :startDate AND :endDate
            """)
    List<ExpenseResponseDto> getExpensesBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
                SELECT COALESCE(SUM(e.price), 0)
                FROM Expense e
                WHERE e.status = true
                AND e.expenseDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal getTotalBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
