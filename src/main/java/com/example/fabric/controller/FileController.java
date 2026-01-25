package com.example.fabric.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fabric.model.StoredFile;
import com.example.fabric.repository.StoredFileRepository;
import com.example.fabric.services.FileStorageService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class FileController {

    private final FileStorageService fileStorageService;
    private final StoredFileRepository storedFileRepository;

    @PostMapping("/storeBill")
    public ResponseEntity<?> storeBill(@RequestBody StoreBillRequest request) {
        try {
            // Decode base64 image data
            String base64Data = request.getImageData();
            if (base64Data.startsWith("data:image/png;base64,")) {
                base64Data = base64Data.substring("data:image/png;base64,".length());
            }
            
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            
            StoredFile storedFile = fileStorageService.storeBill(
                imageBytes, 
                request.getWorkerName(), 
                request.getWeekStartDate(), 
                request.getWorkerId()
            );
            
            return ResponseUtil.createSuccessResponse(storedFile);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Failed to store bill: " + e.getMessage());
        }
    }

    @GetMapping("/bills")
    public ResponseEntity<?> getAllBills() {
        try {
            List<StoredFile> bills = fileStorageService.getAllBills();
            return ResponseUtil.createSuccessResponse(bills);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Failed to retrieve bills: " + e.getMessage());
        }
    }

    @GetMapping("/bills/worker/{workerId}")
    public ResponseEntity<?> getBillsByWorker(@PathVariable Long workerId) {
        try {
            List<StoredFile> bills = fileStorageService.getBillsByWorker(workerId);
            return ResponseUtil.createSuccessResponse(bills);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Failed to retrieve bills: " + e.getMessage());
        }
    }

    @GetMapping("/bills/week/{weekStartDate}")
    public ResponseEntity<?> getBillsByWeek(@PathVariable String weekStartDate) {
        try {
            List<StoredFile> bills = fileStorageService.getBillsByWeek(weekStartDate);
            return ResponseUtil.createSuccessResponse(bills);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Failed to retrieve bills: " + e.getMessage());
        }
    }

    @GetMapping("/excel-uploads")
    public ResponseEntity<?> getAllExcelUploads() {
        try {
            List<StoredFile> uploads = fileStorageService.getAllExcelUploads();
            return ResponseUtil.createSuccessResponse(uploads);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Failed to retrieve Excel uploads: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            // Get file info from database
            StoredFile storedFile = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
            
            Resource resource = fileStorageService.loadFileAsResource(storedFile.getFileName(), storedFile.getFileType());
            
            String contentType = "BILL".equals(storedFile.getFileType()) ? "image/png" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storedFile.getOriginalName() + "\"")
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            boolean deleted = fileStorageService.deleteFile(fileId);
            if (deleted) {
                return ResponseUtil.createSuccessResponse("File deleted successfully");
            } else {
                return ResponseUtil.createErrorResponse(404, "File not found");
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(500, "Failed to delete file: " + e.getMessage());
        }
    }

    // DTO for storing bill request
    public static class StoreBillRequest {
        private String imageData;
        private String workerName;
        private String weekStartDate;
        private Long workerId;

        // Getters and setters
        public String getImageData() { return imageData; }
        public void setImageData(String imageData) { this.imageData = imageData; }
        
        public String getWorkerName() { return workerName; }
        public void setWorkerName(String workerName) { this.workerName = workerName; }
        
        public String getWeekStartDate() { return weekStartDate; }
        public void setWeekStartDate(String weekStartDate) { this.weekStartDate = weekStartDate; }
        
        public Long getWorkerId() { return workerId; }
        public void setWorkerId(Long workerId) { this.workerId = workerId; }
    }
}