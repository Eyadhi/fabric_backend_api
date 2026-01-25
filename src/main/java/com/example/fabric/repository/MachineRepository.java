package com.example.fabric.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fabric.model.Machine;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    Machine findByMachine(String machine);
    Machine findByMachineCode(String machineCode);
}
