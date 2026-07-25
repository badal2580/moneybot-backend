package com.badal.moneybot.dto;

import lombok.Data;

@Data
public class PriceResponse {

    private Data data;

    @lombok.Data
    public static class Data {

        private String base;
        private String currency;
        private String amount;

    }
}
