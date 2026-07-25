package com.badal.moneybot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private Double buyPrice;

    private Double sellPrice;

    private Double currentPrice;

    private Double quantity;

    private Double profit;

    private String status;

    private LocalDateTime createdAt;

    private Double targetPrice;

    private Double stopLoss;
}
