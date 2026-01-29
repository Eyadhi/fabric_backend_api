package com.example.fabric.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.fabric.dto.ExcelMeterUploadResult;
import com.example.fabric.dto.MeterRequest;
import com.example.fabric.repository.WorkerRepository;
import com.example.fabric.repository.MachineRepository;
import com.example.fabric.repository.ProductRepository;
import com.example.fabric.model.Worker;
import com.example.fabric.model.Machine;
import com.example.fabric.model.Product;
import com.example.fabric.model.Piece;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final MeterService meterService;
    private final WorkerRepository workerRepository;
    private final MachineRepository machineRepository;
    private final ProductRepository productRepository;
    private final PieceService pieceService;

    public ExcelMeterUploadResult processExcelFile(MultipartFile file) {
        ExcelMeterUploadResult result = new ExcelMeterUploadResult();
        result.setErrors(new ArrayList<>());

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row if exists
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                // Check if first row looks like a header
                Cell firstCell = headerRow.getCell(0);
                if (firstCell != null && firstCell.getCellType() == CellType.STRING) {
                    String firstCellValue = firstCell.getStringCellValue().toLowerCase();
                    if (firstCellValue.contains("worker") || firstCellValue.contains("id")) {
                        // Skip this header row
                    } else {
                        // Process this row as data
                        processRow(headerRow, result, 1);
                    }
                }
            }

            // Process data rows
            int rowNumber = 2; // Start from row 2 (assuming row 1 was header)
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                processRow(row, result, rowNumber);
                rowNumber++;
            }

            // Set final message
            if (result.getFailedRows() == 0) {
                result.setMessage("All " + result.getSuccessfulRows() + " meters uploaded successfully!");
            } else {
                result.setMessage(result.getSuccessfulRows() + " meters uploaded successfully, " +
                        result.getFailedRows() + " failed. Check errors for details.");
            }

        } catch (IOException e) {
            result.getErrors().add("Error reading Excel file: " + e.getMessage());
            result.setMessage("Failed to process Excel file");
        }

        return result;
    }

    public ExcelMeterUploadResult processPieceExcelFile(MultipartFile file) {
        ExcelMeterUploadResult result = new ExcelMeterUploadResult();
        result.setErrors(new ArrayList<>());

        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row if exists
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                // Check if first row looks like a header
                Cell firstCell = headerRow.getCell(0);
                if (firstCell != null && firstCell.getCellType() == CellType.STRING) {
                    String firstCellValue = firstCell.getStringCellValue().toLowerCase();
                    if (firstCellValue.contains("product") || firstCellValue.contains("code")) {
                        // Skip this header row
                    } else {
                        // Process this row as data
                        processPieceRow(headerRow, result, 1);
                    }
                }
            }

            // Process data rows
            int rowNumber = 2; // Start from row 2 (assuming row 1 was header)
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                processPieceRow(row, result, rowNumber);
                rowNumber++;
            }

            // Set final message
            if (result.getFailedRows() == 0) {
                result.setMessage("All " + result.getSuccessfulRows() + " pieces uploaded successfully!");
            } else {
                result.setMessage(result.getSuccessfulRows() + " pieces uploaded successfully, " +
                        result.getFailedRows() + " failed. Check errors for details.");
            }

        } catch (IOException e) {
            result.getErrors().add("Error reading Excel file: " + e.getMessage());
            result.setMessage("Failed to process Excel file");
        }

        return result;
    }

    private void processPieceRow(Row row, ExcelMeterUploadResult result, int rowNumber) {
        result.incrementTotal();

        try {
            // Extract data from cells
            String productCode = getStringFromCell(row.getCell(0));
            LocalDate exportDate = getDateFromCell(row.getCell(1));
            BigDecimal meters = getBigDecimalFromCell(row.getCell(2));

            // Validate required fields
            if (productCode == null || productCode.trim().isEmpty()) {
                result.getErrors().add("Row " + rowNumber + ": Product Code is required");
                result.incrementFailed();
                return;
            }
            if (exportDate == null) {
                result.getErrors().add("Row " + rowNumber + ": Export Date is required");
                result.incrementFailed();
                return;
            }
            if (meters == null || meters.compareTo(BigDecimal.ZERO) <= 0) {
                result.getErrors().add("Row " + rowNumber + ": Meters must be greater than 0");
                result.incrementFailed();
                return;
            }

            // Find product by code
            Product product = productRepository.findByProductCode(productCode.trim());
            if (product == null) {
                result.getErrors().add("Row " + rowNumber + ": Product not found with code: " + productCode);
                result.incrementFailed();
                return;
            }

            // Create AddPieceDto
            com.example.fabric.dto.AddPieceDto pieceDto = new com.example.fabric.dto.AddPieceDto();
            pieceDto.setProductId(product.getId());
            pieceDto.setExportDate(exportDate);
            pieceDto.setMeters(meters);

            // Save the piece
            pieceService.savePiece(pieceDto);
            result.incrementSuccess();

        } catch (Exception e) {
            result.getErrors().add("Row " + rowNumber + ": " + e.getMessage());
            result.incrementFailed();
        }
    }

    private void processRow(Row row, ExcelMeterUploadResult result, int rowNumber) {
        result.incrementTotal();

        try {
            // Extract data from cells
            String workerCode = getStringFromCell(row.getCell(0));
            String machineCode = getStringFromCell(row.getCell(1));
            LocalDate productionDate = getDateFromCell(row.getCell(2));
            BigDecimal meters = getBigDecimalFromCell(row.getCell(3));
            String productCode = getStringFromCell(row.getCell(4)); // Optional product code

            // Validate required fields
            if (workerCode == null || workerCode.trim().isEmpty()) {
                result.getErrors().add("Row " + rowNumber + ": Worker Code is required");
                result.incrementFailed();
                return;
            }
            if (machineCode == null || machineCode.trim().isEmpty()) {
                result.getErrors().add("Row " + rowNumber + ": Machine Code is required");
                result.incrementFailed();
                return;
            }
            if (productionDate == null) {
                result.getErrors().add("Row " + rowNumber + ": Production Date is required");
                result.incrementFailed();
                return;
            }
            if (meters == null || meters.compareTo(BigDecimal.ZERO) <= 0) {
                result.getErrors().add("Row " + rowNumber + ": Meters must be greater than 0");
                result.incrementFailed();
                return;
            }

            // Find entities by codes
            Worker worker = workerRepository.findByWorkerCode(workerCode.trim());
            if (worker == null) {
                result.getErrors().add("Row " + rowNumber + ": Worker not found with code: " + workerCode);
                result.incrementFailed();
                return;
            }

            Machine machine = machineRepository.findByMachineCode(machineCode.trim());
            if (machine == null) {
                result.getErrors().add("Row " + rowNumber + ": Machine not found with code: " + machineCode);
                result.incrementFailed();
                return;
            }

            Long productId = null;
            if (productCode != null && !productCode.trim().isEmpty()) {
                Product product = productRepository.findByProductCode(productCode.trim());
                if (product == null) {
                    result.getErrors().add("Row " + rowNumber + ": Product not found with code: " + productCode);
                    result.incrementFailed();
                    return;
                }
                productId = product.getId();
            }

            // Create MeterRequest
            MeterRequest meterRequest = new MeterRequest();
            meterRequest.setWorkerId(worker.getId());
            meterRequest.setMachineId(machine.getId());
            meterRequest.setProductionDate(productionDate);
            meterRequest.setMeters(meters);
            meterRequest.setProductId(productId); // Optional - can be null

            // Save the meter
            meterService.saveMeterProduction(meterRequest);
            result.incrementSuccess();

        } catch (Exception e) {
            result.getErrors().add("Row " + rowNumber + ": " + e.getMessage());
            result.incrementFailed();
        }
    }

    private String getStringFromCell(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Convert number to string (useful for codes that might be entered as numbers)
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private BigDecimal getBigDecimalFromCell(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private LocalDate getDateFromCell(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                } else {
                    return null;
                }
            case STRING:
                String dateStr = cell.getStringCellValue().trim();
                return parseDate(dateStr);
            default:
                return null;
        }
    }

    private LocalDate parseDate(String dateStr) {
        // Try different date formats
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }

        return null; // Could not parse date
    }

    /**
     * Generate Excel file with pieces data
     */
    public Workbook generatePiecesExcel(List<Piece> pieces) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Pieces");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Product Code");
        headerRow.createCell(1).setCellValue("Export Date");
        headerRow.createCell(2).setCellValue("Meters");

        // Add data rows
        int rowNum = 1;
        for (Piece piece : pieces) {
            Row row = sheet.createRow(rowNum++);

            // Get product code
            Product product = productRepository.findById(piece.getProductId()).orElse(null);
            String productCode = product != null ? product.getProductCode() : "Unknown";

            row.createCell(0).setCellValue(productCode);
            row.createCell(1).setCellValue(piece.getExporDate().toString());
            row.createCell(2).setCellValue(piece.getMeters().doubleValue());
        }

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    /**
     * Generate Excel template for piece upload
     */
    public Workbook generatePieceTemplate() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Piece Template");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Product Code");
        headerRow.createCell(1).setCellValue("Export Date");
        headerRow.createCell(2).setCellValue("Meters");

        // Add sample data rows
        Row sampleRow1 = sheet.createRow(1);
        sampleRow1.createCell(0).setCellValue("P001");
        sampleRow1.createCell(1).setCellValue("2026-01-15");
        sampleRow1.createCell(2).setCellValue(25.50);

        Row sampleRow2 = sheet.createRow(2);
        sampleRow2.createCell(0).setCellValue("BLUE_FABRIC");
        sampleRow2.createCell(1).setCellValue("2026-01-16");
        sampleRow2.createCell(2).setCellValue(30.00);

        // Auto-size columns
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}