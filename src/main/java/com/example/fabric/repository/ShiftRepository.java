package com.example.fabric.repository;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fabric.model.Shift;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    
    /**
     * Find shift by name (Morning or Night)
     * @param shiftName the shift name
     * @return Optional containing the shift if found
     */
    Shift findByShiftName(String shiftName);
    
    /**
     * Find shift by shift type
     * @param shiftType the shift type number
     * @return Shift if found
     */
    Shift findByShiftType(int shiftType);
    
    /**
     * Check if shift type already exists
     * @param shiftType the shift type number
     * @return true if exists
     */
    boolean existsByShiftType(int shiftType);
    
    /**
     * Find all shifts ordered by shift name
     * @return List of shifts ordered by shift name
     */
    List<Shift> findAllByOrderByShiftName();

    @Query("""
        SELECT s FROM Shift s
        WHERE s.startTime < :endTime
          AND s.endTime > :startTime
    """)
    List<Shift> findOverlappingShifts(
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}