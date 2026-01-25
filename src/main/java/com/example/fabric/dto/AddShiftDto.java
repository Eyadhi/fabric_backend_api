package com.example.fabric.dto;

import lombok.Data;

@Data
public class AddShiftDto {
    private String shiftName;
    private String startTime;
    private String endTime;
    private String shiftType;
    private String description;
}
