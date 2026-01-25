package com.example.fabric.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fabric.model.Worker;
import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findByWorkerName(String workerName);
    Worker findByWorkerCode(String workerCode);
}
