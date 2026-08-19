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

import com.example.fabric.exceptions.ResourceNotFoundException;
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
        String base64Data = request.getImageData();
        if (base64Data.startsWith("data:image/png;base64,")) {
            base64Data = base64Data.substring("data:image/png;base64,".length());
        }

        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

        StoredFile storedFile = fileStorageService.storeBill(
                imageBytes,
                request.getWorkerName(),
                request.getWeekStartDate(),
                request.getWorkerId());

        return ResponseUtil.createSuccessResponse(storedFile);
    }

    @GetMapping("/bills")
    public ResponseEntity<?> getAllBills() {
        return ResponseUtil.createSuccessResponse(fileStorageService.getAllBills());
    }

    @GetMapping("/bills/worker/{workerId}")
    public ResponseEntity<?> getBillsByWorker(@PathVariable Long workerId) {
        List<StoredFile> bills = fileStorageService.getBillsByWorker(workerId);
        return ResponseUtil.createSuccessResponse(bills);
    }

    @GetMapping("/bills/week/{weekStartDate}")
    public ResponseEntity<?> getBillsByWeek(@PathVariable String weekStartDate) {
        List<StoredFile> bills = fileStorageService.getBillsByWeek(weekStartDate);
        return ResponseUtil.createSuccessResponse(bills);
    }

    @GetMapping("/excel-uploads")
    public ResponseEntity<?> getAllExcelUploads() {
        return ResponseUtil.createSuccessResponse(fileStorageService.getAllExcelUploads());
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        StoredFile storedFile = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File", fileId));

        Resource resource = fileStorageService.loadFileAsResource(
                storedFile.getFileName(), storedFile.getFileType());

        String contentType = "BILL".equals(storedFile.getFileType())
                ? "image/png"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + storedFile.getOriginalName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        boolean deleted = fileStorageService.deleteFile(fileId);
        if (!deleted) {
            throw new ResourceNotFoundException("File", fileId);
        }
        return ResponseUtil.createSuccessResponse("File deleted successfully");
    }

    public static class StoreBillRequest {
        private String imageData;
        private String workerName;
        private String weekStartDate;
        private Long   workerId;

        public String getImageData()       { return imageData; }
        public void   setImageData(String v)       { this.imageData = v; }

        public String getWorkerName()      { return workerName; }
        public void   setWorkerName(String v)      { this.workerName = v; }

        public String getWeekStartDate()   { return weekStartDate; }
        public void   setWeekStartDate(String v)   { this.weekStartDate = v; }

        public Long   getWorkerId()        { return workerId; }
        public void   setWorkerId(Long v)  { this.workerId = v; }
    }
}
