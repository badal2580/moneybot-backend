package com.badal.moneybot.service;

import com.badal.moneybot.client.CoinbaseClient;
import com.badal.moneybot.dto.*;
import com.badal.moneybot.entity.Trade;
import com.badal.moneybot.entity.User;
import com.badal.moneybot.repository.TradeRepository;
import com.badal.moneybot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TradeService {

    private static final Logger log =
            LoggerFactory.getLogger(TradeService.class);

    private final TradeRepository repository;
    private final CoinbaseClient coinbaseClient;
    private final TelegramService telegramService;
    private final UserRepository userRepository;

    public TradeService(
            TradeRepository repository,
            CoinbaseClient coinbaseClient,
            TelegramService telegramService,
            UserRepository userRepository
    ) {
        this.repository = repository;
        this.coinbaseClient = coinbaseClient;
        this.telegramService = telegramService;
        this.userRepository = userRepository;
    }

    // =====================================================
    // PRICE HELPER
    // =====================================================

    private double getLivePrice(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new RuntimeException("Trading symbol is required");
        }

        return Double.parseDouble(
                coinbaseClient
                        .getCoinPrice(symbol)
                        .getData()
                        .getAmount()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // =====================================================
    // PAGINATED TRADE HISTORY
    // =====================================================

    public Page<Trade> getTradeHistory(
            int page,
            int size,
            String sortBy
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort
                        .by(sortBy)
                        .descending()
        );

        String email = getCurrentUser().getEmail();

        return repository.findByUserEmail(email, pageable);
    }

    // =====================================================
    // SEARCH TRADES
    // =====================================================

    public List<Trade> searchTrades(
            String status,
            String symbol
    ) {

        String email = getCurrentUser().getEmail();

        List<Trade> userTrades =
                repository.findByUserEmail(email);

        return userTrades.stream()
                .filter(trade ->
                        status == null ||
                                status.isBlank() ||
                                status.equalsIgnoreCase(
                                        trade.getStatus()
                                )
                )
                .filter(trade ->
                        symbol == null ||
                                symbol.isBlank() ||
                                symbol.equalsIgnoreCase(
                                        trade.getSymbol()
                                )
                )
                .toList();
    }

    // =====================================================
    // TEST SAVE TRADE
    // =====================================================

    public Trade saveTrade() {

        String symbol = "BTC-USD";

        double currentPrice = getLivePrice(symbol);

        log.info(
                "Current {} price: {}",
                symbol,
                currentPrice
        );

        Trade trade = Trade.builder()
                .symbol(symbol)
                .buyPrice(currentPrice)
                .currentPrice(currentPrice)
                .sellPrice(0.0)
                .quantity(1.0)
                .profit(0.0)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .user(getCurrentUser())
                .build();

        return repository.save(trade);
    }

    // =====================================================
    // SELL TRADE BY ID
    // =====================================================

    public Trade sellTrade(Long id) {

        String email = getCurrentUser().getEmail();

        Trade trade = repository.findByIdAndUserEmail(
                id,
                email
        ).orElseThrow(() ->
                new RuntimeException(
                        "Trade not found or access denied"
                )
        );

        if (!"OPEN".equalsIgnoreCase(trade.getStatus())) {
            throw new RuntimeException(
                    "Trade is already closed"
            );
        }

        double currentPrice =
                getLivePrice(trade.getSymbol());

        trade.setSellPrice(currentPrice);
        trade.setCurrentPrice(currentPrice);

        double profit =
                (currentPrice - trade.getBuyPrice())
                        * trade.getQuantity();

        trade.setProfit(round(profit));
        trade.setStatus("CLOSED");

        Trade savedTrade = repository.save(trade);

        sendTradeClosedMessage(savedTrade);

        return savedTrade;
    }

    // =====================================================
    // GET ALL USER TRADES
    // =====================================================

    public List<Trade> getAllTrades() {

        String email = getCurrentUser().getEmail();

        return repository.findByUserEmail(email);
    }

    // =====================================================
    // PORTFOLIO SUMMARY
    // =====================================================

    public PortfolioSummaryResponse getPortfolioSummary() {

        String email = getCurrentUser().getEmail();

        List<Trade> trades =
                repository.findByUserEmail(email);

        double totalInvestment = 0.0;
        double currentPortfolioValue = 0.0;
        double unrealizedProfitLoss = 0.0;
        double realizedProfitLoss = 0.0;

        long openTrades = 0;
        long closedTrades = 0;

        for (Trade trade : trades) {

            if ("OPEN".equalsIgnoreCase(
                    trade.getStatus()
            )) {

                openTrades++;

                double buyPrice =
                        trade.getBuyPrice() == null
                                ? 0
                                : trade.getBuyPrice();

                double currentPrice =
                        trade.getCurrentPrice() == null
                                ? buyPrice
                                : trade.getCurrentPrice();

                double quantity =
                        trade.getQuantity() == null
                                ? 0
                                : trade.getQuantity();

                double investment =
                        buyPrice * quantity;

                double currentValue =
                        currentPrice * quantity;

                totalInvestment += investment;
                currentPortfolioValue += currentValue;

                unrealizedProfitLoss +=
                        currentValue - investment;

            } else if (
                    "CLOSED".equalsIgnoreCase(
                            trade.getStatus()
                    ) ||
                            "SUCCESS".equalsIgnoreCase(
                                    trade.getStatus()
                            )
            ) {

                closedTrades++;

                if (trade.getProfit() != null) {
                    realizedProfitLoss +=
                            trade.getProfit();
                }
            }
        }

        double totalProfitLoss =
                unrealizedProfitLoss
                        + realizedProfitLoss;

        double roiPercentage =
                totalInvestment > 0
                        ? (totalProfitLoss
                        / totalInvestment) * 100
                        : 0.0;

        return PortfolioSummaryResponse.builder()
                .totalInvestment(
                        round(totalInvestment)
                )
                .currentPortfolioValue(
                        round(currentPortfolioValue)
                )
                .unrealizedProfitLoss(
                        round(unrealizedProfitLoss)
                )
                .realizedProfitLoss(
                        round(realizedProfitLoss)
                )
                .totalProfitLoss(
                        round(totalProfitLoss)
                )
                .roiPercentage(
                        round(roiPercentage)
                )
                .openTrades(openTrades)
                .closedTrades(closedTrades)
                .build();
    }

    // =====================================================
    // BUY TRADE
    // =====================================================

    public Trade buyTrade(BuyTradeRequest request) {

        if (request.getSymbol() == null ||
                request.getSymbol().isBlank()) {

            throw new RuntimeException(
                    "Trading symbol is required"
            );
        }

        if (request.getQuantity() == null ||
                request.getQuantity() <= 0) {

            throw new RuntimeException(
                    "Quantity must be greater than zero"
            );
        }

        String symbol =
                request.getSymbol()
                        .trim()
                        .toUpperCase();

        double currentPrice =
                getLivePrice(symbol);

        Trade trade = Trade.builder()
                .symbol(symbol)
                .buyPrice(currentPrice)
                .currentPrice(currentPrice)
                .sellPrice(0.0)
                .quantity(request.getQuantity())
                .targetPrice(request.getTargetPrice())
                .stopLoss(request.getStopLoss())
                .profit(0.0)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .user(getCurrentUser())
                .build();

        Trade savedTrade =
                repository.save(trade);

        telegramService.sendMessage(
                "🟢 BUY EXECUTED\n\n" +
                        "Trade ID: " +
                        savedTrade.getId() + "\n" +
                        "Coin: " +
                        savedTrade.getSymbol() + "\n" +
                        "Buy Price: " +
                        String.format(
                                "%.2f",
                                savedTrade.getBuyPrice()
                        ) + "\n" +
                        "Quantity: " +
                        savedTrade.getQuantity() + "\n" +
                        "Target: " +
                        savedTrade.getTargetPrice() + "\n" +
                        "Stop Loss: " +
                        savedTrade.getStopLoss()
        );

        return savedTrade;
    }

    // =====================================================
    // SELL TRADE USING REQUEST
    // =====================================================

    public Trade sellTrade(
            SellTradeRequest request
    ) {

        if (request.getTradeId() == null) {
            throw new RuntimeException(
                    "Trade ID is required"
            );
        }

        return sellTrade(request.getTradeId());
    }

    // =====================================================
    // OPEN TRADES
    // =====================================================

    public List<Trade> getOpenTrades() {

        String email = getCurrentUser().getEmail();

        return repository
                .findByUserEmailAndStatus(
                        email,
                        "OPEN"
                );
    }

    // =====================================================
    // CLOSED TRADES
    // =====================================================

    public List<Trade> getClosedTrades() {

        String email = getCurrentUser().getEmail();

        return repository
                .findByUserEmailAndStatus(
                        email,
                        "CLOSED"
                );
    }

    // =====================================================
    // BTC PRICE
    // =====================================================

    public String getBitcoinPrice() {

        return coinbaseClient
                .getCoinPrice("BTC-USD")
                .getData()
                .getAmount();
    }

    // =====================================================
    // AUTO TRADING ENGINE
    // =====================================================

    public void checkLivePrice() {

        List<Trade> openTrades =
                repository.findByStatus("OPEN");

        log.info(
                "Open trades found: {}",
                openTrades.size()
        );

        for (Trade trade : openTrades) {

            try {

                if (trade.getSymbol() == null ||
                        trade.getSymbol().isBlank()) {

                    log.warn(
                            "Trade {} skipped because symbol is missing",
                            trade.getId()
                    );

                    continue;
                }

                if (trade.getBuyPrice() == null ||
                        trade.getQuantity() == null) {

                    log.warn(
                            "Trade {} skipped because buy price or quantity is missing",
                            trade.getId()
                    );

                    continue;
                }

                double currentPrice =
                        getLivePrice(
                                trade.getSymbol()
                        );

                log.info(
                        "Checking {} live price: {}",
                        trade.getSymbol(),
                        currentPrice
                );

                trade.setCurrentPrice(
                        currentPrice
                );

                double profit =
                        (currentPrice
                                - trade.getBuyPrice())
                                * trade.getQuantity();

                trade.setProfit(
                        round(profit)
                );

                String closingReason = null;

                if (
                        trade.getTargetPrice() != null &&
                                currentPrice >=
                                        trade.getTargetPrice()
                ) {

                    closingReason =
                            "🎯 TARGET HIT";

                } else if (
                        trade.getStopLoss() != null &&
                                currentPrice <=
                                        trade.getStopLoss()
                ) {

                    closingReason =
                            "🛑 STOP LOSS HIT";
                }

                if (closingReason != null) {

                    trade.setSellPrice(
                            currentPrice
                    );

                    trade.setStatus(
                            "CLOSED"
                    );

                    Trade savedTrade =
                            repository.save(trade);

                    log.info(
                            "{} for trade ID {}",
                            closingReason,
                            savedTrade.getId()
                    );

                    telegramService.sendMessage(
                            closingReason + "\n\n" +
                                    "Trade ID: " +
                                    savedTrade.getId() + "\n" +
                                    "Coin: " +
                                    savedTrade.getSymbol() + "\n" +
                                    "Buy Price: " +
                                    String.format(
                                            "%.2f",
                                            savedTrade.getBuyPrice()
                                    ) + "\n" +
                                    "Sell Price: " +
                                    String.format(
                                            "%.2f",
                                            savedTrade.getSellPrice()
                                    ) + "\n" +
                                    "Quantity: " +
                                    savedTrade.getQuantity() + "\n" +
                                    "Profit/Loss: " +
                                    String.format(
                                            "%.2f",
                                            savedTrade.getProfit()
                                    )
                    );

                } else {

                    repository.save(trade);

                    log.info(
                            "Trade {} updated. Symbol: {}, Current P/L: {}",
                            trade.getId(),
                            trade.getSymbol(),
                            trade.getProfit()
                    );
                }

            } catch (Exception exception) {

                log.error(
                        "Failed to process trade ID {}: {}",
                        trade.getId(),
                        exception.getMessage(),
                        exception
                );
            }
        }
    }

    // =====================================================
    // PROFIT CHART
    // =====================================================

    public List<ProfitChartResponse> getProfitChart() {

        String email = getCurrentUser().getEmail();

        List<Trade> trades =
                repository
                        .findByUserEmailAndStatusOrderByCreatedAtAsc(
                                email,
                                "CLOSED"
                        );

        return trades.stream()
                .map(trade ->
                        new ProfitChartResponse(
                                trade.getCreatedAt()
                                        .toLocalDate()
                                        .toString(),
                                trade.getProfit() == null
                                        ? 0.0
                                        : trade.getProfit()
                        )
                )
                .toList();
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    public DashboardResponse getDashboard() {

        String email =
                getCurrentUser().getEmail();

        double livePrice =
                getLivePrice("BTC-USD");

        List<Trade> userTrades =
                repository.findByUserEmail(email);

        double totalProfit =
                userTrades.stream()
                        .mapToDouble(trade ->
                                trade.getProfit() == null
                                        ? 0.0
                                        : trade.getProfit()
                        )
                        .sum();

        return DashboardResponse.builder()
                .totalTrades(
                        repository.countByUserEmail(
                                email
                        )
                )
                .openTrades(
                        repository
                                .countByUserEmailAndStatus(
                                        email,
                                        "OPEN"
                                )
                )
                .closedTrades(
                        repository
                                .countByUserEmailAndStatus(
                                        email,
                                        "CLOSED"
                                )
                )
                .totalProfit(
                        round(totalProfit)
                )
                .livePrice(livePrice)
                .build();
    }

    // =====================================================
    // SIMPLE TRADE HISTORY
    // =====================================================

    public List<Trade> getTradeHistory() {

        String email =
                getCurrentUser().getEmail();

        return repository
                .findByUserEmailOrderByCreatedAtDesc(
                        email
                );
    }

    // =====================================================
    // TRADE STATISTICS
    // =====================================================

    public TradeStatisticsResponse getTradeStatistics() {

        String email =
                getCurrentUser().getEmail();

        List<Trade> userTrades =
                repository.findByUserEmail(email);

        List<Trade> closedTrades =
                userTrades.stream()
                        .filter(trade ->
                                "CLOSED".equalsIgnoreCase(
                                        trade.getStatus()
                                )
                        )
                        .toList();

        long totalTrades =
                userTrades.size();

        long openTrades =
                userTrades.stream()
                        .filter(trade ->
                                "OPEN".equalsIgnoreCase(
                                        trade.getStatus()
                                )
                        )
                        .count();

        long closedTradeCount =
                closedTrades.size();

        long winningTrades =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null &&
                                        trade.getProfit() > 0
                        )
                        .count();

        long losingTrades =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null &&
                                        trade.getProfit() < 0
                        )
                        .count();

        double totalProfit =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null &&
                                        trade.getProfit() > 0
                        )
                        .mapToDouble(
                                Trade::getProfit
                        )
                        .sum();

        double totalLoss =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null &&
                                        trade.getProfit() < 0
                        )
                        .mapToDouble(
                                Trade::getProfit
                        )
                        .sum();

        double netProfit =
                totalProfit + totalLoss;

        double winRate =
                closedTradeCount == 0
                        ? 0
                        : (winningTrades * 100.0)
                        / closedTradeCount;

        return TradeStatisticsResponse.builder()
                .totalTrades(totalTrades)
                .openTrades(openTrades)
                .closedTrades(closedTradeCount)
                .winningTrades(winningTrades)
                .losingTrades(losingTrades)
                .totalProfit(round(totalProfit))
                .totalLoss(round(totalLoss))
                .netProfit(round(netProfit))
                .winRate(round(winRate))
                .build();
    }

    // =====================================================
    // CURRENT USER
    // =====================================================

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (
                authentication == null ||
                        !authentication.isAuthenticated() ||
                        "anonymousUser".equals(
                                authentication.getPrincipal()
                        )
        ) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }

        String email =
                authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Logged-in user not found"
                        )
                );
    }

    // =====================================================
    // TRADE ANALYTICS
    // =====================================================

    public TradeAnalyticsResponse getTradeAnalytics() {

        String email =
                getCurrentUser().getEmail();

        List<Trade> closedTrades =
                repository
                        .findByUserEmailAndStatus(
                                email,
                                "CLOSED"
                        );

        long totalTrades =
                closedTrades.size();

        long winningTrades =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null &&
                                        trade.getProfit() > 0
                        )
                        .count();

        long losingTrades =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null &&
                                        trade.getProfit() < 0
                        )
                        .count();

        double winRate =
                totalTrades == 0
                        ? 0
                        : (winningTrades * 100.0)
                        / totalTrades;

        double bestTrade =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null
                        )
                        .mapToDouble(
                                Trade::getProfit
                        )
                        .max()
                        .orElse(0);

        double worstTrade =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null
                        )
                        .mapToDouble(
                                Trade::getProfit
                        )
                        .min()
                        .orElse(0);

        double averageProfit =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null &&
                                        trade.getProfit() > 0
                        )
                        .mapToDouble(
                                Trade::getProfit
                        )
                        .average()
                        .orElse(0);

        double averageLoss =
                closedTrades.stream()
                        .filter(trade ->
                                trade.getProfit() != null &&
                                        trade.getProfit() < 0
                        )
                        .mapToDouble(
                                Trade::getProfit
                        )
                        .average()
                        .orElse(0);

        return TradeAnalyticsResponse.builder()
                .totalTrades(totalTrades)
                .winningTrades(winningTrades)
                .losingTrades(losingTrades)
                .winRate(round(winRate))
                .bestTrade(round(bestTrade))
                .worstTrade(round(worstTrade))
                .averageProfit(
                        round(averageProfit)
                )
                .averageLoss(
                        round(averageLoss)
                )
                .build();
    }

    // =====================================================
    // TELEGRAM HELPER
    // =====================================================

    private void sendTradeClosedMessage(
            Trade trade
    ) {

        String emoji =
                trade.getProfit() != null &&
                        trade.getProfit() >= 0
                        ? "🟢"
                        : "🔴";

        telegramService.sendMessage(
                emoji + " TRADE CLOSED\n\n" +
                        "Trade ID: " +
                        trade.getId() + "\n" +
                        "Coin: " +
                        trade.getSymbol() + "\n" +
                        "Buy Price: " +
                        String.format(
                                "%.2f",
                                trade.getBuyPrice()
                        ) + "\n" +
                        "Sell Price: " +
                        String.format(
                                "%.2f",
                                trade.getSellPrice()
                        ) + "\n" +
                        "Quantity: " +
                        trade.getQuantity() + "\n" +
                        "Profit: " +
                        String.format(
                                "%.2f",
                                trade.getProfit()
                        )
        );
    }
}
