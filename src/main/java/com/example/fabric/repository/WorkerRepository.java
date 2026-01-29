package com.example.fabric.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.fabric.model.Worker;
import com.example.fabric.projection.WorkerListView;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findByWorkerName(String workerName);

    Worker findByWorkerCode(String workerCode);

    @Query("""
                SELECT w.id AS id,
                       w.workerCode AS workerCode,
                       w.workerName AS workerName,
                       w.mobile AS mobile
                FROM Worker w
            """)
    List<WorkerListView> findAllWorkersView();

    @Query("""
                SELECT w.id AS id,
                       w.workerCode AS workerCode,
                       w.workerName AS workerName,
                       w.mobile AS mobile
                FROM Worker w
                WHERE w.id = :id
            """)
    List<WorkerListView> findWorkerByIdView(@Param("id") Long id);

}
