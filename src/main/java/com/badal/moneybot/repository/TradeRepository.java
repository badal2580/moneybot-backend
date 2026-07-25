package com.badal.moneybot.repository;

import com.badal.moneybot.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TradeRepository
        extends JpaRepository<Trade, Long>,
        JpaSpecificationExecutor<Trade> {

    List<Trade> findByStatus(String status);

    List<Trade> findBySymbol(String symbol);

    List<Trade> findByStatusAndSymbol(String status, String symbol);

    long countByStatus(String status);

    List<Trade> findAllByOrderByCreatedAtDesc();

    long countByProfitGreaterThan(double profit);

    long countByProfitLessThan(double profit);

    List<Trade> findByStatusOrderByCreatedAtAsc(String status);
}
