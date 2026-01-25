package com.example.fabric.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(name = "worker_shift_assignments", 
       uniqueConstraints = {
           @UniqueConstraint(name = "unique_worker_machine_period", 
                           columnNames = {"worker_id", "machine_id", "period_start_date", "period_end_date"})
       },
       indexes = {
           @Index(name = "idx_worker_period", columnList = "worker_id, period_start_date, period_end_date"),
           @Index(name = "idx_machine_period", columnList = "machine_id, period_start_date, period_end_date"),
           @Index(name = "idx_period_dates", columnList = "period_start_date, period_end_date"),
           @Index(name = "idx_active_assignments", columnList = "is_active"),
       })
public class WorkerShiftAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Machine machine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Shift shift; // References shift type

    @Column(name = "assignment_type", nullable = false)
    private int assignmentType; // WEEKLY or CUSTOM_RANGE

    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate; // Start of assignment period

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate; // End of assignment period

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Helper methods
    public String getShiftName() {
        return shift != null ? shift.getShiftName() : null;
    }
    
    public Long getShiftId() {
        return shift != null ? shift.getId() : null;
    }
}