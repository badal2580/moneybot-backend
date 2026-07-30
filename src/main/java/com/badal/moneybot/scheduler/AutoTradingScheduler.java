package com.badal.moneybot.scheduler;

import com.badal.moneybot.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoTradingScheduler {

    private final TradeService tradeService;

    @Scheduled(fixedDelay = 30000)
    public void monitorOpenTrades() {

        try {

            log.info("Auto trading engine started");

            tradeService.checkLivePrice();

            log.info("Auto trading engine completed");

        } catch (Exception exception) {

            log.error(
                    "Auto trading engine failed: {}",
                    exception.getMessage(),
                    exception
            );

        }
    }
}
