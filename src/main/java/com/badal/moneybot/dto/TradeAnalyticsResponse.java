package com.badal.moneybot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeAnalyticsResponse {

    private long totalTrades;

    private long winningTrades;

    private long losingTrades;

    private double winRate;

    private double bestTrade;

    private double worstTrade;

    private double averageProfit;

    private double averageLoss;
}
