package com.badal.moneybot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalTrades;

    private long openTrades;

    private long closedTrades;

    private double totalProfit;

    private double livePrice;

}
