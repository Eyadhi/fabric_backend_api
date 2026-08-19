package com.example.fabric.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.fabric.exceptions.DuplicateResourceException;
import com.example.fabric.exceptions.ResourceNotFoundException;
import com.example.fabric.model.Machine;
import com.example.fabric.projection.MachineListView;
import com.example.fabric.repository.MachineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MachineService {
    private final MachineRepository machineRepository;

    public Machine saveMachine(Machine machine) {
        Machine existingMachine = machineRepository.findByMachine(machine.getMachine());
        if (existingMachine != null) {
            throw new DuplicateResourceException("Machine already exists: " + machine.getMachine());
        }
        return machineRepository.save(machine);
    }

    public List<MachineListView> getAllMachines() {
        return machineRepository.findAllMachinesView();
    }

    public List<MachineListView> getMachineById(Long id) {
        return machineRepository.findMachineByIdView(id);
    }
}
