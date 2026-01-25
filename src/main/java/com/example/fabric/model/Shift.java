package com.example.fabric.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "shifts")
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_name", nullable = false, unique = true)
    private String shiftName; // "Morning" or "Night"

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "description")
    private String description;

    @Column(name = "shift_type", unique = true)
    private int shiftType;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructor for easy creation
    public Shift() {}
    
    public Shift(String shiftName, LocalTime startTime, LocalTime endTime, String description) {
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }
}
