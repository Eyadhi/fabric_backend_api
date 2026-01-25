package com.example.fabric.dto;

import lombok.Data;

@Data
public class UpdateWorkerDto {
    private Long id;
    private String workerCode;
    private String workerName;
    private String mobile;
}