package com.example.fabric.services;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.fabric.model.Machine;
import com.example.fabric.repository.MachineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MachineService {
    private final MachineRepository machineRepository;

    public Machine saveMachine(Machine machine) {
        Machine existingMachine = machineRepository.findByMachine(machine.getMachine());

        if (existingMachine != null) {
            throw new RuntimeException("Machine already exists: " + machine.getMachine());
        }
        return machineRepository.save(machine);
    }

    public List<Machine> getAllMachines() {
        return machineRepository.findAll();
    }

    public List<Machine> getMachineById(Long id) {
        return machineRepository.findById(id).map(Collections::singletonList).orElse(Collections.emptyList());
    }
}
