package com.badal.moneybot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfitChartResponse {

    private String date;
    private Double profit;

}
