package com.example.fabric.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fabric.model.StoredFile;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {
    
    List<StoredFile> findByFileTypeOrderByCreatedAtDesc(String fileType);
    
    List<StoredFile> findByWorkerIdAndFileTypeOrderByCreatedAtDesc(Long workerId, String fileType);
    
    List<StoredFile> findByWeekStartDateAndFileTypeOrderByCreatedAtDesc(String weekStartDate, String fileType);
    
    List<StoredFile> findByWorkerIdAndWeekStartDateAndFileTypeOrderByCreatedAtDesc(
        Long workerId, String weekStartDate, String fileType);
}