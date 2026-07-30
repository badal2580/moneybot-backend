package com.badal.moneybot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummaryResponse {

    private Double totalInvestment;
    private Double currentPortfolioValue;
    private Double unrealizedProfitLoss;
    private Double realizedProfitLoss;
    private Double totalProfitLoss;
    private Double roiPercentage;
    private Long openTrades;
    private Long closedTrades;
}
