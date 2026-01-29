package com.example.fabric.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
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
import com.example.fabric.model.StoredFile;
import com.example.fabric.projection.MachineListView;
import com.example.fabric.services.ExcelService;
import com.example.fabric.services.FileStorageService;
import com.example.fabric.services.MachineService;
import com.example.fabric.services.MeterService;
import com.example.fabric.services.ProductMachineService;
import com.example.fabric.util.ResponseUtil;

import jakarta.servlet.http.HttpServletRequest;
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
        try {
            List<MachineListView> machines = (id != null) ? machineService.getMachineById(id)
                    : machineService.getAllMachines();
            return ResponseUtil.createSuccessResponse(machines);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @GetMapping("/getMachineMeters")
    public ResponseEntity<?> getMachineMeters(
            @RequestParam Long machineId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<Meter> meters;

            if (startDate != null && endDate != null) {
                // Get meters for specific date range
                LocalDate start = LocalDate.parse(startDate);
                LocalDate end = LocalDate.parse(endDate);
                meters = meterService.getMachineProductionByDateRange(machineId, start, end);
            } else if (startDate != null) {
                // Get weekly production starting from startDate
                LocalDate weekStart = LocalDate.parse(startDate);
                meters = meterService.getMachineWeeklyProduction(machineId, weekStart);
            } else {
                // Get all meters for the machine (current behavior)
                meters = meterService.getMetersByMachineId(machineId);
            }

            return ResponseUtil.createSuccessResponse(meters);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @GetMapping("/getMachineRunningProduct")
    public ResponseEntity<?> getMachineRunningProduct(@RequestParam Long machineId) {
        try {
            // Get running products for this machine using the new ProductMachineService
            List<ProductMachineDto> runningProducts = productMachineService.getProductMachinesByStatus(1)
                    .stream()
                    .filter(pm -> pm.getMachineId().equals(machineId))
                    .toList();

            if (runningProducts.isEmpty()) {
                return ResponseUtil.createErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "No running product found for this machine");
            }

            // Return the first running product (there should typically be only one)
            return ResponseUtil.createSuccessResponse(runningProducts.get(0));
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @GetMapping("/getMachineProducts")
    public ResponseEntity<?> getMachineProducts(@RequestParam Long machineId) {
        try {
            // Get all products for this machine using the new ProductMachineService
            List<ProductMachineDto> machineProducts = productMachineService.getAllProductMachines()
                    .stream()
                    .filter(pm -> pm.getMachineId().equals(machineId))
                    .toList();

            return ResponseUtil.createSuccessResponse(machineProducts);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error fetching machine products: " + ex.getMessage());
        }
    }

    @PostMapping("/addMachine")
    public ResponseEntity<?> saveMachine(@RequestBody Machine machine, HttpServletRequest httpRequest) {
        try {
            Machine savedMachine = machineService.saveMachine(machine);
            return ResponseUtil.createSuccessResponse(savedMachine);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error adding Machine: " + e.getMessage());
        }
    }

    @PostMapping("/uploadMetersExcel")
    public ResponseEntity<?> uploadMetersExcel(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseUtil.createErrorResponse(400, "Please select a file to upload");
            }

            // Check file extension
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
                return ResponseUtil.createErrorResponse(400, "Please upload an Excel file (.xlsx)");
            }

            // Store the Excel file first
            String description = "Meter data upload - " + fileName + " (" + file.getSize() + " bytes)";
            StoredFile storedFile = fileStorageService.storeExcelFile(file, description);

            // Process the Excel file
            ExcelMeterUploadResult result = excelService.processExcelFile(file);

            // Add file storage info to result
            result.setStoredFileId(storedFile.getId());
            result.setStoredFileName(storedFile.getFileName());

            if (result.getFailedRows() == 0) {
                return ResponseUtil.createSuccessResponse(result);
            } else {
                // Some rows failed, but some might have succeeded
                return ResponseEntity.status(207) // 207 Multi-Status
                        .body(ResponseUtil.createSuccessResponse(result));
            }

        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(500, "Error processing Excel file: " + ex.getMessage());
        }
    }
}
