package com.example.fabric.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.fabric.dto.ExpenseTypeDropdownDto;
import com.example.fabric.model.ExpenseType;

public interface ExpenseTypeRepository extends JpaRepository<ExpenseType, Long> {
    Optional<ExpenseType> findByTypeIgnoreCase(String type);

    @Query("""
                SELECT new com.example.fabric.dto.ExpenseTypeDropdownDto(
                    e.id,
                    e.type
                )
                FROM ExpenseType e
                WHERE e.status = true
                ORDER BY e.type
            """)
    List<ExpenseTypeDropdownDto> getExpenseTypeDropdown();
}
