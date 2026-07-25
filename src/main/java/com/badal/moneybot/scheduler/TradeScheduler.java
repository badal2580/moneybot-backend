package com.badal.moneybot.scheduler;

import com.badal.moneybot.service.TradeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TradeScheduler {

    private final TradeService tradeService;

    public TradeScheduler(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @Scheduled(fixedRate = 5000)
    public void checkPrice() {

        System.out.println("--------------------------------");
        System.out.println("Checking Live BTC Price...");
        tradeService.checkLivePrice();
        System.out.println("--------------------------------");

    }
}
