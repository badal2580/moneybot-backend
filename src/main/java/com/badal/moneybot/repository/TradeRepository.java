package com.badal.moneybot.repository;

import com.badal.moneybot.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TradeRepository
        extends JpaRepository<Trade, Long>,
        JpaSpecificationExecutor<Trade> {

    List<Trade> findByStatus(String status);

    List<Trade> findByUserEmail(String email);

    List<Trade> findByUserEmailAndStatus(
            String email,
            String status
    );

    List<Trade> findByUserEmailAndStatusOrderByCreatedAtAsc(
            String email,
            String status
    );

    List<Trade> findByUserEmailOrderByCreatedAtDesc(
            String email
    );

    Page<Trade> findByUserEmail(
            String email,
            Pageable pageable
    );

    long countByUserEmail(String email);

    long countByUserEmailAndStatus(
            String email,
            String status
    );

    Optional<Trade> findByIdAndUserEmail(
            Long id,
            String email
    );
}
