package com.example.fabric.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fabric.model.WorkerShiftAssignment;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<WorkerShiftAssignment, Long> {
        
    // Find all active assignments for a worker
    List<WorkerShiftAssignment> findByWorkerIdAndIsActive(Long workerId, Boolean isActive);
    
    // Check if worker is already assigned to a specific week and shift (any machine)
    boolean existsByWorkerIdAndPeriodStartDateAndShift_IdAndIsActive(
        Long workerId, LocalDate periodStartDate, Long shiftId, Boolean isActive);

    // Check if worker is already assigned to a specific machine and week
    boolean existsByWorkerIdAndMachineIdAndPeriodStartDateAndIsActive(
        Long workerId, Long machineId, LocalDate periodStartDate, Boolean isActive);
    
    // Find weekly assignments for multiple workers
    List<WorkerShiftAssignment> findByWorkerIdInAndPeriodStartDateAndIsActive(
        List<Long> workerIds, LocalDate periodStartDate, Boolean isActive);
    
    // Find assignments by period range
    List<WorkerShiftAssignment> findByPeriodStartDateBetweenAndIsActive(
        LocalDate startDate, LocalDate endDate, Boolean isActive);
    
    // Deactivate a specific assignment
    @Modifying
    @Query("UPDATE WorkerShiftAssignment w SET w.isActive = false WHERE w.id = :assignmentId")
    void deactivateAssignment(@Param("assignmentId") Long assignmentId);
    
    // Find assignments with worker and machine details
    @Query("SELECT w FROM WorkerShiftAssignment w " +
           "JOIN FETCH w.worker " +
           "JOIN FETCH w.machine " +
           "JOIN FETCH w.shift " +
           "WHERE w.periodStartDate = :periodStartDate AND w.isActive = :isActive")
    List<WorkerShiftAssignment> findByWeekStartDateWithDetails(
        @Param("periodStartDate") LocalDate periodStartDate, 
        @Param("isActive") Boolean isActive);
}