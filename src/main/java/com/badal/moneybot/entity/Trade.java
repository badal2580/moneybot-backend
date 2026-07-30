package com.badal.moneybot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trades",
        indexes = {
                @Index(name = "idx_trade_user", columnList = "user_id"),
                @Index(name = "idx_trade_status", columnList = "status"),
                @Index(name = "idx_trade_symbol", columnList = "symbol")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(nullable = false, length = 20)
    private String symbol;

    @Column(nullable = false)
    private Double buyPrice;

    private Double sellPrice;

    @Column(nullable = false)
    private Double currentPrice;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double profit;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Double targetPrice;

    private Double stopLoss;

    @PrePersist
    public void beforeSave() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "OPEN";
        }

        if (profit == null) {
            profit = 0.0;
        }

        if (sellPrice == null) {
            sellPrice = 0.0;
        }
    }
}
