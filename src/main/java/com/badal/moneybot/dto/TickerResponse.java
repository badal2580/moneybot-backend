package com.badal.moneybot.dto;

import lombok.Data;

@Data
public class TickerResponse {

    private String symbol;

    private Double buyPrice;

    private Double sellPrice;
}
