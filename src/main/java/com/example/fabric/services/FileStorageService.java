package com.example.fabric.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.fabric.model.StoredFile;
import com.example.fabric.repository.StoredFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StoredFileRepository storedFileRepository;

    @Value("${app.file.storage.path:./storage}")
    private String storageBasePath;

    private static final String BILLS_FOLDER = "bills";
    private static final String EXCEL_UPLOADS_FOLDER = "excel_uploads";

    public void initializeStorage() {
        try {
            Path billsPath = Paths.get(storageBasePath, BILLS_FOLDER);
            Path excelPath = Paths.get(storageBasePath, EXCEL_UPLOADS_FOLDER);
            
            Files.createDirectories(billsPath);
            Files.createDirectories(excelPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directories", e);
        }
    }

    public StoredFile storeBill(byte[] billData, String workerName, String weekStartDate, Long workerId) {
        try {
            initializeStorage();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("bill_%s_%s_%s.png", 
                workerName.replaceAll("\\s+", "_"), weekStartDate, timestamp);
            
            Path billsDir = Paths.get(storageBasePath, BILLS_FOLDER);
            Path filePath = billsDir.resolve(fileName);
            
            Files.write(filePath, billData);
            
            StoredFile storedFile = new StoredFile();
            storedFile.setFileName(fileName);
            storedFile.setOriginalName(fileName);
            storedFile.setFilePath(filePath.toString());
            storedFile.setFileType("BILL");
            storedFile.setFileSize((long) billData.length);
            storedFile.setWorkerId(workerId);
            storedFile.setWeekStartDate(weekStartDate);
            storedFile.setDescription("Production bill for " + workerName + " - Week " + weekStartDate);
            storedFile.setCreatedBy("system");
            
            return storedFileRepository.save(storedFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store bill file", e);
        }
    }

    public StoredFile storeExcelFile(MultipartFile file, String description) {
        try {
            initializeStorage();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null && originalFileName.contains(".") 
                ? originalFileName.substring(originalFileName.lastIndexOf(".")) 
                : ".xlsx";
            
            String fileName = String.format("meters_upload_%s_%s%s", 
                timestamp, UUID.randomUUID().toString().substring(0, 8), fileExtension);
            
            Path excelDir = Paths.get(storageBasePath, EXCEL_UPLOADS_FOLDER);
            Path filePath = excelDir.resolve(fileName);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            StoredFile storedFile = new StoredFile();
            storedFile.setFileName(fileName);
            storedFile.setOriginalName(originalFileName);
            storedFile.setFilePath(filePath.toString());
            storedFile.setFileType("EXCEL_UPLOAD");
            storedFile.setFileSize(file.getSize());
            storedFile.setDescription(description != null ? description : "Meter data upload");
            storedFile.setCreatedBy("system");
            
            return storedFileRepository.save(storedFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store Excel file", e);
        }
    }

    public Resource loadFileAsResource(String fileName, String fileType) {
        try {
            String folder = "BILL".equals(fileType) ? BILLS_FOLDER : EXCEL_UPLOADS_FOLDER;
            Path filePath = Paths.get(storageBasePath, folder).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException("File not found: " + fileName, e);
        }
    }

    public List<StoredFile> getAllBills() {
        return storedFileRepository.findByFileTypeOrderByCreatedAtDesc("BILL");
    }

    public List<StoredFile> getAllExcelUploads() {
        return storedFileRepository.findByFileTypeOrderByCreatedAtDesc("EXCEL_UPLOAD");
    }

    public List<StoredFile> getBillsByWorker(Long workerId) {
        return storedFileRepository.findByWorkerIdAndFileTypeOrderByCreatedAtDesc(workerId, "BILL");
    }

    public List<StoredFile> getBillsByWeek(String weekStartDate) {
        return storedFileRepository.findByWeekStartDateAndFileTypeOrderByCreatedAtDesc(weekStartDate, "BILL");
    }

    public boolean deleteFile(Long fileId) {
        try {
            StoredFile storedFile = storedFileRepository.findById(fileId).orElse(null);
            if (storedFile != null) {
                Path filePath = Paths.get(storedFile.getFilePath());
                Files.deleteIfExists(filePath);
                storedFileRepository.delete(storedFile);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}