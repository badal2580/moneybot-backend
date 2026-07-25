package com.badal.moneybot.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class BuyTradeRequest {

    @NotBlank(message = "Symbol is required")
    private String symbol;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;

    @NotNull(message = "Target Price is required")
    @Positive(message = "Target Price must be greater than 0")
    private Double targetPrice;

    @NotNull(message = "Stop Loss is required")
    @Positive(message = "Stop Loss must be greater than 0")
    private Double stopLoss;





}
