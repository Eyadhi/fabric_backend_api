package com.example.fabric.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", unique = true)
    private String productCode;

    @Column(name = "product_name")
    private String productName;

    private String description;

    private int meters;

    private int costin;

    private int costout;

    // Overall product lifecycle tracking (aggregated from ProductMachine entries)
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // Earliest start date across all machines

    @Column(name = "end_date")
    private LocalDate endDate; // Latest end date when completed on all machines

    @Column(name = "is_complete", nullable = false)
    private int isComplete = 1; // 1 = running on at least one machine, 2 = completed on all machines

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "metersout", precision = 12, scale = 2)
    private BigDecimal metersout;

    @Column(name = "point_decrease", precision = 5, scale = 2)
    private BigDecimal pointDecrease = BigDecimal.ZERO; // Percentage to decrease from total meters (e.g., 5.50 for 5.5%)
}
