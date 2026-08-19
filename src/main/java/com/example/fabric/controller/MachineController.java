package com.example.fabric.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fabric.dto.ExcelMeterUploadResult;
import com.example.fabric.dto.ProductMachineDto;
import com.example.fabric.model.Machine;
import com.example.fabric.model.Meter;
import com.example.fabric.projection.MachineListView;
import com.example.fabric.services.ExcelService;
import com.example.fabric.services.FileStorageService;
import com.example.fabric.services.MachineService;
import com.example.fabric.services.MeterService;
import com.example.fabric.services.ProductMachineService;
import com.example.fabric.util.ResponseUtil;
import com.example.fabric.exceptions.BadRequestException;
import com.example.fabric.exceptions.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;
    private final MeterService meterService;
    private final ProductMachineService productMachineService;
    private final ExcelService excelService;
    private final FileStorageService fileStorageService;

    @GetMapping("/getMachine")
    public ResponseEntity<?> getMachine(@RequestParam(value = "id", required = false) Long id) {
        List<MachineListView> machines = (id != null)
                ? machineService.getMachineById(id)
                : machineService.getAllMachines();
        return ResponseUtil.createSuccessResponse(machines);
    }

    @GetMapping("/getMachineMeters")
    public ResponseEntity<?> getMachineMeters(
            @RequestParam Long machineId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        List<Meter> meters;
        if (startDate != null && endDate != null) {
            meters = meterService.getMachineProductionByDateRange(
                    machineId, LocalDate.parse(startDate), LocalDate.parse(endDate));
        } else if (startDate != null) {
            meters = meterService.getMachineWeeklyProduction(machineId, LocalDate.parse(startDate));
        } else {
            meters = meterService.getMetersByMachineId(machineId);
        }
        return ResponseUtil.createSuccessResponse(meters);
    }

    @GetMapping("/getMachineRunningProduct")
    public ResponseEntity<?> getMachineRunningProduct(@RequestParam Long machineId) {
        List<ProductMachineDto> running = productMachineService.getProductMachinesByStatus(1)
                .stream()
                .filter(pm -> pm.getMachineId().equals(machineId))
                .toList();

        if (running.isEmpty()) {
            throw new ResourceNotFoundException("No running product found for machine " + machineId);
        }
        return ResponseUtil.createSuccessResponse(running.get(0));
    }

    @GetMapping("/getMachineProducts")
    public ResponseEntity<?> getMachineProducts(@RequestParam Long machineId) {
        List<ProductMachineDto> machineProducts = productMachineService.getAllProductMachines()
                .stream()
                .filter(pm -> pm.getMachineId().equals(machineId))
                .toList();
        return ResponseUtil.createSuccessResponse(machineProducts);
    }

    @PostMapping("/addMachine")
    public ResponseEntity<?> saveMachine(@RequestBody Machine machine) {
        Machine savedMachine = machineService.saveMachine(machine);
        return ResponseUtil.createSuccessResponse(savedMachine);
    }

    @PostMapping("/uploadMetersExcel")
    public ResponseEntity<?> uploadMetersExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            throw new BadRequestException("Please upload an Excel file (.xlsx)");
        }

        // Store the file then process it — both may throw FileProcessingException
        var storedFile = fileStorageService.storeExcelFile(file,
                "Meter data upload - " + fileName + " (" + file.getSize() + " bytes)");

        ExcelMeterUploadResult result = excelService.processExcelFile(file);
        result.setStoredFileId(storedFile.getId());
        result.setStoredFileName(storedFile.getFileName());

        // 207 Multi-Status when some rows failed but others succeeded
        if (result.getFailedRows() > 0) {
            return ResponseEntity.status(207).body(ResponseUtil.createSuccessResponse(result));
        }
        return ResponseUtil.createSuccessResponse(result);
    }
}
