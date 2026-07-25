package com.badal.moneybot.service;

import com.badal.moneybot.client.CoinbaseClient;
import com.badal.moneybot.dto.*;
import com.badal.moneybot.entity.Trade;
import com.badal.moneybot.repository.TradeRepository;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

@Service
public class TradeService {

    private static final Logger log =
            LoggerFactory.getLogger(TradeService.class);

    private final TradeRepository repository;
    private final CoinbaseClient coinbaseClient;

    private final TelegramService telegramService;

    public TradeService(TradeRepository repository, CoinbaseClient coinbaseClient, TelegramService telegramService) {
        this.repository = repository;
        this.coinbaseClient = coinbaseClient;
        this.telegramService = telegramService;
    }

    public Page<Trade> getTradeHistory(int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.by(sortBy).descending()
        );

        return repository.findAll(pageable);
    }

    public List<Trade> searchTrades(String status, String symbol) {

        if (status != null && symbol != null) {
            return repository.findByStatusAndSymbol(status, symbol);
        }

        if (status != null) {
            return repository.findByStatus(status);
        }

        if (symbol != null) {
            return repository.findBySymbol(symbol);
        }

        return repository.findAll();

    }

    public Trade saveTrade() {

        Double currentPrice = Double.parseDouble(
                coinbaseClient
                        .getBitcoinPrice()
                        .getData()
                        .getAmount()
        );

        log.info("Current BTC Price : {}", currentPrice);

        double profit = 0.0;

        Trade trade = Trade.builder()
                .symbol("BTC-USD")
                .buyPrice(currentPrice)
                .currentPrice(currentPrice)
                .sellPrice(0.0)
                .quantity(1.0)
                .profit(profit)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(trade);
    }
    public Trade sellTrade(Long id) {

        Trade trade = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trade Not Found"));

        Double currentPrice = Double.parseDouble(
                coinbaseClient
                        .getBitcoinPrice()
                        .getData()
                        .getAmount()
        );

        trade.setSellPrice(currentPrice);
        trade.setCurrentPrice(currentPrice);

        double profit =
                (currentPrice - trade.getBuyPrice())
                        * trade.getQuantity();

        trade.setProfit(profit);
        trade.setStatus("CLOSED");

        return repository.save(trade);
    }

    public List<Trade> getAllTrades() {
        return repository.findAll();
    }

    public Trade buyTrade(BuyTradeRequest request) {

        Double currentPrice = Double.parseDouble(
                coinbaseClient
                        .getBitcoinPrice()
                        .getData()
                        .getAmount()
        );
        Trade trade = Trade.builder()
                .symbol(request.getSymbol())
                .buyPrice(currentPrice)
                .currentPrice(currentPrice)
                .sellPrice(0.0)
                .quantity(request.getQuantity())
                .targetPrice(request.getTargetPrice())
                .stopLoss(request.getStopLoss())
                .profit(0.0)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();
        Trade savedTrade =  repository.save(trade);

        telegramService.sendMessage(
                "🟢 BUY EXECUTED\n\n" +
                        "Coin: " + trade.getSymbol() + "\n" +
                        "Buy Price: " + trade.getBuyPrice() + "\n" +
                        "Quantity: " + trade.getQuantity() + "\n" +
                        "Target: " + trade.getTargetPrice() + "\n" +
                        "Stop Loss: " + trade.getStopLoss()
        );

        return savedTrade;
    }
    public Trade sellTrade(SellTradeRequest request) {

        Trade trade = repository.findById(request.getTradeId())
                .orElseThrow(() -> new RuntimeException("Trade not found"));

        Double currentPrice = Double.parseDouble(
                coinbaseClient
                        .getBitcoinPrice()
                        .getData()
                        .getAmount()
        );

        trade.setSellPrice(currentPrice);
        trade.setCurrentPrice(currentPrice);

        double profit = (currentPrice - trade.getBuyPrice()) * trade.getQuantity();
        trade.setProfit(profit);

        trade.setStatus("CLOSED");

        Trade savedTrade = repository.save(trade);

        String emoji = savedTrade.getProfit() >= 0 ? "🟢" : "🔴";

        telegramService.sendMessage(
                emoji + " TRADE CLOSED\n\n" +
                        "Coin: " + savedTrade.getSymbol() + "\n" +
                        "Buy Price: " + savedTrade.getBuyPrice() + "\n" +
                        "Sell Price: " + savedTrade.getSellPrice() + "\n" +
                        "Quantity: " + savedTrade.getQuantity() + "\n" +
                        "Profit: " + String.format("%.2f", savedTrade.getProfit())
        );

        return savedTrade;
    }

    public List<Trade> getOpenTrades() {

        return repository.findByStatus("OPEN");

    }
    public List<Trade> getClosedTrades() {

        return repository.findByStatus("CLOSED");

    }

    public String getBitcoinPrice() {

        return coinbaseClient
                .getBitcoinPrice()
                .getData()
                .getAmount();

    }

    public void checkLivePrice() {

        Double currentPrice = Double.parseDouble(
                coinbaseClient
                        .getBitcoinPrice()
                        .getData()
                        .getAmount()
        );

        log.info("Checking Live BTC Price...");
        log.info("Live BTC Price : {}", currentPrice);

        List<Trade> trades = repository.findByStatus("OPEN");

        log.info("Open Trades : {}", trades.size());

        for (Trade trade : trades) {

            log.info("----------------------");
            log.info("Trade Id : {}", trade.getId());
            log.info("Buy Price : {}", trade.getBuyPrice());
            log.info("Current Price : {}", currentPrice);

            trade.setCurrentPrice(currentPrice);

            double profit =
                    (currentPrice - trade.getBuyPrice()) * trade.getQuantity();

            trade.setProfit(profit);

            log.info("Profit : {}", profit);

            // Target Hit
            if (trade.getTargetPrice() != null && currentPrice >= trade.getTargetPrice()) {

                trade.setSellPrice(currentPrice);
                trade.setStatus("CLOSED");

                log.info("🎯 TARGET HIT");
                log.info("Trade Closed Successfully");

            }

            // Stop Loss Hit
            else if (trade.getStopLoss() != null && currentPrice <= trade.getStopLoss()) {

                trade.setSellPrice(currentPrice);
                trade.setStatus("CLOSED");

                log.info("🛑 STOP LOSS HIT");
                log.info("Trade Closed Successfully");

            }

            repository.save(trade);

        }

    }

    public List<ProfitChartResponse> getProfitChart() {

        List<Trade> trades =
                repository.findByStatusOrderByCreatedAtAsc("CLOSED");

        return trades.stream()
                .map(trade -> new ProfitChartResponse(
                        trade.getCreatedAt().toLocalDate().toString(),
                        trade.getProfit()
                ))
                .toList();
    }

    public DashboardResponse getDashboard() {

        Double livePrice = Double.parseDouble(
                coinbaseClient
                        .getBitcoinPrice()
                        .getData()
                        .getAmount()
        );

        double totalProfit = repository.findAll()
                .stream()
                .mapToDouble(Trade::getProfit)
                .sum();

        return DashboardResponse.builder()
                .totalTrades(repository.count())
                .openTrades(repository.countByStatus("OPEN"))
                .closedTrades(repository.countByStatus("CLOSED"))
                .totalProfit(totalProfit)
                .livePrice(livePrice)
                .build();

    }

    public List<Trade> getTradeHistory() {

        return repository.findAllByOrderByCreatedAtDesc();

    }

    public TradeStatisticsResponse getTradeStatistics() {

        long totalTrades = repository.count();

        long openTrades = repository.countByStatus("OPEN");

        long closedTrades = repository.countByStatus("CLOSED");

        long winningTrades = repository.countByProfitGreaterThan(0);

        long losingTrades = repository.countByProfitLessThan(0);

        double totalProfit = repository.findAll()
                .stream()
                .filter(t -> t.getProfit() > 0)
                .mapToDouble(Trade::getProfit)
                .sum();

        double totalLoss = repository.findAll()
                .stream()
                .filter(t -> t.getProfit() < 0)
                .mapToDouble(Trade::getProfit)
                .sum();

        double netProfit = totalProfit + totalLoss;

        double winRate = totalTrades == 0
                ? 0
                : (winningTrades * 100.0) / totalTrades;

        return TradeStatisticsResponse.builder()
                .totalTrades(totalTrades)
                .openTrades(openTrades)
                .closedTrades(closedTrades)
                .winningTrades(winningTrades)
                .losingTrades(losingTrades)
                .totalProfit(totalProfit)
                .totalLoss(totalLoss)
                .netProfit(netProfit)
                .winRate(winRate)
                .build();

    }
}
