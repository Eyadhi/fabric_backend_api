package com.example.fabric.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.fabric.model.Machine;
import com.example.fabric.projection.MachineListView;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    Machine findByMachine(String machine);

    Machine findByMachineCode(String machineCode);

    @Query("""
                SELECT w.id AS id,
                       w.machineCode AS machineCode,
                       w.machine AS machine
                    FROM Machine w
            """)
    List<MachineListView> findAllMachinesView();

    @Query("""
                SELECT w.id AS id,
                       w.machineCode AS machineCode,
                       w.machine AS machine
                    FROM Machine w
                WHERE w.id = :id
            """)
    List<MachineListView> findMachineByIdView(@Param("id") Long id);

}
