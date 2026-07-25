package com.badal.moneybot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeStatisticsResponse {

    private long totalTrades;

    private long openTrades;

    private long closedTrades;

    private long winningTrades;

    private long losingTrades;

    private double totalProfit;

    private double totalLoss;

    private double netProfit;

    private double winRate;

}
