package com.example.fabric.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.fabric.dto.AddPieceDto;
import com.example.fabric.model.Piece;
import com.example.fabric.repository.PieceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PieceService {

    private final PieceRepository pieceRepository;

    public Piece savePiece(AddPieceDto dto) {
        Piece piece = new Piece();
        piece.setProductId(dto.getProductId());
        piece.setMeters(dto.getMeters());
        piece.setExporDate(dto.getExportDate());
        return pieceRepository.save(piece);
    }

    public List<Piece> getAllPieces() {
        return pieceRepository.findAll();
    }

    public List<Piece> getPieceById(Long id) {
        return pieceRepository.findById(id).map(Collections::singletonList).orElse(Collections.emptyList());
    }

    public List<Piece> getPiecesByProductId(Long productId) {
        return pieceRepository.findByProductId(productId);
    }

    public Map<LocalDate, BigDecimal> getDateWiseTotalMeters(Long productId) {
        List<Piece> pieces = pieceRepository.findByProductId(productId);
        return pieces.stream()
                .collect(Collectors.groupingBy(
                    Piece::getExporDate,
                    Collectors.reducing(BigDecimal.ZERO, Piece::getMeters, BigDecimal::add)
                ));
    }

    public BigDecimal getTotalMetersByProduct(Long productId) {
        List<Piece> pieces = pieceRepository.findByProductId(productId);
        return pieces.stream()
                .map(Piece::getMeters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
