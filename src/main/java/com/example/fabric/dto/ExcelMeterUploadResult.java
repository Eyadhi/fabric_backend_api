package com.example.fabric.dto;

import java.util.List;

import lombok.Data;

@Data
public class ExcelMeterUploadResult {
    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private List<String> errors;
    private String message;
    private Long storedFileId;
    private String storedFileName;

    public ExcelMeterUploadResult() {
        this.totalRows = 0;
        this.successfulRows = 0;
        this.failedRows = 0;
    }

    public void incrementSuccess() {
        this.successfulRows++;
    }

    public void incrementFailed() {
        this.failedRows++;
    }

    public void incrementTotal() {
        this.totalRows++;
    }
}