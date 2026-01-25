package com.example.fabric.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.fabric.dto.AddPieceDto;
import com.example.fabric.dto.ExcelMeterUploadResult;
import com.example.fabric.model.Piece;
import com.example.fabric.services.ExcelService;
import com.example.fabric.services.PieceService;
import com.example.fabric.util.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class PieceController {

    private final PieceService pieceService;
    private final ExcelService excelService;

    @GetMapping("/getPiece")
    public List<Piece> getPiece(@RequestParam(value = "id", required = false) Long id,
                               @RequestParam(value = "productId", required = false) Long productId) {
        if (id != null) {
            return pieceService.getPieceById(id);
        } else if (productId != null) {
            return pieceService.getPiecesByProductId(productId);
        } else {
            return pieceService.getAllPieces();
        }
    }

    @PostMapping("/addPieces")
    public ResponseEntity<?> savePiece(@RequestBody AddPieceDto addPieceDto) {
        try {
            Piece savedPiece = pieceService.savePiece(addPieceDto);
            return ResponseUtil.createSuccessResponse(savedPiece);

        } catch (IllegalArgumentException ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.CONFLICT.value(),
                    ex.getMessage());

        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Unexpected error: " + ex.getMessage());
        }
    }

    @PostMapping("/uploadPieceExcel")
    public ResponseEntity<?> uploadPieceExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseUtil.createErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Please select a file to upload");
            }

            // Check file type
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ResponseUtil.createErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Please upload a valid Excel file (.xlsx or .xls)");
            }

            ExcelMeterUploadResult result = excelService.processPieceExcelFile(file);
            
            if (result.getFailedRows() == 0) {
                return ResponseUtil.createSuccessResponse(result);
            } else {
                // Partial success - return 207 Multi-Status
                return ResponseEntity.status(207).body(result);
            }

        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error processing Excel file: " + ex.getMessage());
        }
    }

    @GetMapping("/downloadPieceExcel")
    public ResponseEntity<byte[]> downloadPieceExcel(@RequestParam(value = "productId", required = false) Long productId) {
        try {
            List<Piece> pieces;
            String filename;
            
            if (productId != null) {
                pieces = pieceService.getPiecesByProductId(productId);
                filename = "pieces_product_" + productId + ".xlsx";
            } else {
                pieces = pieceService.getAllPieces();
                filename = "all_pieces.xlsx";
            }

            Workbook workbook = excelService.generatePiecesExcel(pieces);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/downloadPieceTemplate")
    public ResponseEntity<byte[]> downloadPieceTemplate() {
        try {
            Workbook workbook = excelService.generatePieceTemplate();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "piece_upload_template.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getPieceStatistics")
    public ResponseEntity<?> getPieceStatistics(@RequestParam Long productId) {
        try {
            Map<LocalDate, BigDecimal> dateWiseMeters = pieceService.getDateWiseTotalMeters(productId);
            BigDecimal totalMeters = pieceService.getTotalMetersByProduct(productId);
            
            var response = new java.util.HashMap<String, Object>();
            response.put("dateWiseMeters", dateWiseMeters);
            response.put("totalMeters", totalMeters);
            response.put("productId", productId);
            
            return ResponseUtil.createSuccessResponse(response);
        } catch (Exception ex) {
            return ResponseUtil.createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Error fetching piece statistics: " + ex.getMessage());
        }
    }
}
