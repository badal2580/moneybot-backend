package com.badal.moneybot.controller;

import com.badal.moneybot.constant.ApiMessages;
import com.badal.moneybot.dto.*;
import com.badal.moneybot.entity.Trade;
import com.badal.moneybot.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Trade API",
        description = "Authenticated crypto paper-trading operations"
)
@RestController
@RequestMapping("/trade")
@SecurityRequirement(name = "bearerAuth")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @Operation(
            summary = "Create buy trade",
            description = "Creates a trade using the selected coin's live market price."
    )
    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<Trade>> buyTrade(
            @Valid @RequestBody BuyTradeRequest request
    ) {

        Trade trade = tradeService.buyTrade(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ApiMessages.TRADE_CREATED,
                        trade
                )
        );
    }

    @Operation(
            summary = "Sell trade",
            description = "Closes a logged-in user's open trade."
    )
    @PutMapping("/sell/{id}")
    public ResponseEntity<ApiResponse<Trade>> sellTrade(
            @PathVariable Long id
    ) {

        Trade trade = tradeService.sellTrade(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trade sold successfully",
                        trade
                )
        );
    }

    @Operation(
            summary = "Sell trade using request body"
    )
    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<Trade>> sellTrade(
            @Valid @RequestBody SellTradeRequest request
    ) {

        Trade trade = tradeService.sellTrade(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trade sold successfully",
                        trade
                )
        );
    }

    @Operation(summary = "Get logged-in user's trades")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Trade>>> getAllTrades() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trades fetched successfully",
                        tradeService.getAllTrades()
                )
        );
    }

    @Operation(summary = "Get live Bitcoin price")
    @GetMapping("/price")
    public ResponseEntity<ApiResponse<String>> getBitcoinPrice() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Live Bitcoin price fetched successfully",
                        tradeService.getBitcoinPrice()
                )
        );
    }

    @Operation(summary = "Get open trades")
    @GetMapping("/open")
    public ResponseEntity<ApiResponse<List<Trade>>> getOpenTrades() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Open trades fetched successfully",
                        tradeService.getOpenTrades()
                )
        );
    }

    @Operation(summary = "Get closed trades")
    @GetMapping("/closed")
    public ResponseEntity<ApiResponse<List<Trade>>> getClosedTrades() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Closed trades fetched successfully",
                        tradeService.getClosedTrades()
                )
        );
    }

    @Operation(
            summary = "Get dashboard",
            description = "Returns trade counts, profit and current BTC price."
    )
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dashboard fetched successfully",
                        tradeService.getDashboard()
                )
        );
    }

    @Operation(
            summary = "Get paginated trade history"
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<Trade>>> getHistory(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "5")
            int size,

            @RequestParam(defaultValue = "createdAt")
            String sort
    ) {

        Page<Trade> trades =
                tradeService.getTradeHistory(
                        page,
                        size,
                        sort
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trade history fetched successfully",
                        trades
                )
        );
    }

    @Operation(
            summary = "Search trades",
            description = "Filters the logged-in user's trades by status and symbol."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Trade>>> searchTrades(

            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            String symbol
    ) {

        List<Trade> trades =
                tradeService.searchTrades(
                        status,
                        symbol
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trades fetched successfully",
                        trades
                )
        );
    }

    @Operation(summary = "Get trade statistics")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<TradeStatisticsResponse>>
    getStatistics() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trade statistics fetched successfully",
                        tradeService.getTradeStatistics()
                )
        );
    }

    @Operation(summary = "Get profit chart data")
    @GetMapping("/profit-chart")
    public ResponseEntity<ApiResponse<List<ProfitChartResponse>>>
    getProfitChart() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profit chart fetched successfully",
                        tradeService.getProfitChart()
                )
        );
    }

    @Operation(summary = "Get portfolio summary")
    @GetMapping("/portfolio-summary")
    public ResponseEntity<ApiResponse<PortfolioSummaryResponse>>
    getPortfolioSummary() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Portfolio summary fetched successfully",
                        tradeService.getPortfolioSummary()
                )
        );
    }

    @Operation(summary = "Get trade analytics")
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<TradeAnalyticsResponse>>
    getAnalytics() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trade analytics fetched successfully",
                        tradeService.getTradeAnalytics()
                )
        );
    }
}
