package com.example.fabric.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fabric.model.Meter;

@Repository
public interface MeterRepository extends JpaRepository<Meter, Long> {
    List<Meter> findByProductionDate(LocalDate productionDate);

    List<Meter> findByWorkerId(Long workerId);

    List<Meter> findByMachineId(Long machineId);

    List<Meter> findByWorkerIdAndMachineIdAndProductId(Long workerId, Long machineId, Long productId);

    List<Meter> findByWorkerIdAndMachineIdAndProductIdAndProductionDateBetween(Long workerId, Long machineId,
            Long productId, LocalDate start, LocalDate end);

    List<Meter> findByWorkerIdAndProductIdAndProductionDateBetween(Long workerId, Long productId, LocalDate start,
            LocalDate end);

    List<Meter> findByWorkerIdAndProductionDateBetween(Long workerId, LocalDate start, LocalDate end);

    List<Meter> findByMachineIdAndProductionDateBetween(Long machineId, LocalDate start, LocalDate end);
}
