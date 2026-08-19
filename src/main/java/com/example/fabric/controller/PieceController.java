package com.example.fabric.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
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
import com.example.fabric.exceptions.BadRequestException;
import com.example.fabric.exceptions.FileProcessingException;
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
    public List<Piece> getPiece(
            @RequestParam(value = "id",        required = false) Long id,
            @RequestParam(value = "productId", required = false) Long productId) {
        if (id != null)        return pieceService.getPieceById(id);
        if (productId != null) return pieceService.getPiecesByProductId(productId);
        return pieceService.getAllPieces();
    }

    @PostMapping("/addPieces")
    public ResponseEntity<?> savePiece(@RequestBody AddPieceDto addPieceDto) {
        // IllegalArgumentException → DuplicateResourceException (409) via GlobalExceptionHandler
        Piece savedPiece = pieceService.savePiece(addPieceDto);
        return ResponseUtil.createSuccessResponse(savedPiece);
    }

    @PostMapping("/uploadPieceExcel")
    public ResponseEntity<?> uploadPieceExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("Please select a file to upload");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BadRequestException("Please upload a valid Excel file (.xlsx or .xls)");
        }

        ExcelMeterUploadResult result = excelService.processPieceExcelFile(file);

        if (result.getFailedRows() > 0) {
            return ResponseEntity.status(207).body(result);
        }
        return ResponseUtil.createSuccessResponse(result);
    }

    @GetMapping("/downloadPieceExcel")
    public ResponseEntity<byte[]> downloadPieceExcel(
            @RequestParam(value = "productId", required = false) Long productId) {

        List<Piece> pieces;
        String filename;

        if (productId != null) {
            pieces   = pieceService.getPiecesByProductId(productId);
            filename = "pieces_product_" + productId + ".xlsx";
        } else {
            pieces   = pieceService.getAllPieces();
            filename = "all_pieces.xlsx";
        }

        return buildExcelResponse(excelService.generatePiecesExcel(pieces), filename);
    }

    @GetMapping("/downloadPieceTemplate")
    public ResponseEntity<byte[]> downloadPieceTemplate() {
        return buildExcelResponse(excelService.generatePieceTemplate(), "piece_upload_template.xlsx");
    }

    @GetMapping("/getPieceStatistics")
    public ResponseEntity<?> getPieceStatistics(@RequestParam Long productId) {
        Map<LocalDate, BigDecimal> dateWiseMeters = pieceService.getDateWiseTotalMeters(productId);
        BigDecimal totalMeters = pieceService.getTotalMetersByProduct(productId);

        Map<String, Object> response = new HashMap<>();
        response.put("dateWiseMeters", dateWiseMeters);
        response.put("totalMeters",    totalMeters);
        response.put("productId",      productId);

        return ResponseUtil.createSuccessResponse(response);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<byte[]> buildExcelResponse(Workbook workbook, String filename) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok().headers(headers).body(out.toByteArray());
        } catch (IOException ex) {
            throw new FileProcessingException("Failed to generate Excel file: " + ex.getMessage(), ex);
        }
    }
}
