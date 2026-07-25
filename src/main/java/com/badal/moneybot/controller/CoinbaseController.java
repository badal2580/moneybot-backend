package com.badal.moneybot.controller;

import com.badal.moneybot.service.TradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoinbaseController {

    private final TradeService tradeService;

    public CoinbaseController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/btc")
    public String getBitcoinPrice() {
        return tradeService.getBitcoinPrice();
    }
}
