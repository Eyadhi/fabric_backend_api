package com.example.fabric.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fabric.model.Piece;

public interface PieceRepository extends JpaRepository<Piece, Long> {
    List<Piece> findByProductId(Long productId);
}
