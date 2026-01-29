package com.example.fabric.services;

import org.springframework.stereotype.Service;
import com.example.fabric.dto.DashboardStatsDto;
import com.example.fabric.repository.WorkerRepository;
import com.example.fabric.repository.MachineRepository;
import com.example.fabric.repository.ProductRepository;
import com.example.fabric.repository.ShiftRepository;
import com.example.fabric.repository.PieceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WorkerRepository workerRepository;
    private final MachineRepository machineRepository;
    private final ProductRepository productRepository;
    private final ShiftRepository shiftRepository;
    private final PieceRepository pieceRepository;

    public DashboardStatsDto getDashboardStats() {
        long totalWorkers = workerRepository.count();
        long totalMachines = machineRepository.count();
        long totalProducts = productRepository.count();
        long totalShifts = shiftRepository.count();
        long totalPieces = pieceRepository.count();

        return new DashboardStatsDto(
                totalWorkers,
                totalMachines,
                totalProducts,
                totalShifts,
                totalPieces);
    }
}
