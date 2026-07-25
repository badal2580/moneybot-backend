package com.badal.moneybot.controller;

import com.badal.moneybot.constant.ApiMessages;
import com.badal.moneybot.dto.*;
import com.badal.moneybot.entity.Trade;
import com.badal.moneybot.service.TelegramService;
import com.badal.moneybot.service.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.CrossOrigin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Trade API", description = "Operations related to crypto trading")
@RestController
@RequestMapping("/trade")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://moneybot-frontend-s5zo.vercel.app"
})
public class TradeController {

    private final TradeService tradeService;

    private final TelegramService telegramService;

    public TradeController(TradeService tradeService, TelegramService telegramService) {
        this.tradeService = tradeService;
        this.telegramService = telegramService;
    }

    @Operation(summary = "Create Demo Trade")
    @PostMapping("/save")
    public ApiResponse<Trade> saveTrade() {

        Trade trade = tradeService.saveTrade();

        return ApiResponse.<Trade>builder()
                .success(true)
                .message(ApiMessages.TRADE_CREATED)
                .data(trade)
                .build();
    }

    @Operation(
            summary = "Create Buy Trade",
            description = "Creates a new crypto trade using live Coinbase price."
    )
    @PostMapping("/buy")
    public ApiResponse<Trade> buyTrade(@Valid @RequestBody BuyTradeRequest request) {

        Trade trade = tradeService.buyTrade(request);

        return ApiResponse.<Trade>builder()
                .success(true)
                .message(ApiMessages.TRADE_CREATED)
                .data(trade)
                .build();
    }

    @Operation(summary = "Sell Trade using Request Body")
    @PostMapping("/sell")
    public ApiResponse<Trade> sellTrade(@RequestBody SellTradeRequest request) {

        Trade trade = tradeService.sellTrade(request);

        return ApiResponse.<Trade>builder()
                .success(true)
                .message("Trade sold successfully")
                .data(trade)
                .build();
    }

    @Operation(summary = "Sell an Existing Trade")
    @PutMapping("/sell/{id}")
    public ApiResponse<Trade> sellTrade(@PathVariable Long id) {

        Trade trade = tradeService.sellTrade(id);

        return ApiResponse.<Trade>builder()
                .success(true)
                .message("Trade sold successfully")
                .data(trade)
                .build();
    }

    @Operation(summary = "Get All Trades")
    @GetMapping("/all")
    public ApiResponse<List<Trade>> getAllTrades() {

        return ApiResponse.<List<Trade>>builder()
                .success(true)
                .message("All trades fetched successfully")
                .data(tradeService.getAllTrades())
                .build();
    }

    @Operation(summary = "Test Trade Controller")
    @GetMapping("/btc")
    public ApiResponse<String> test() {

        return ApiResponse.<String>builder()
                .success(true)
                .message("Controller is working")
                .data("BTC Controller Working")
                .build();
    }

    @Operation(summary = "Get Live Bitcoin Price")
    @GetMapping("/price")
    public String getPrice() {
        return tradeService.getBitcoinPrice();
    }


    @Operation(summary = "Get Latest Live Bitcoin Price")
    @GetMapping("/latest")
    public ApiResponse<String> latestPrice() {

        return ApiResponse.<String>builder()
                .success(true)
                .message("Live Bitcoin price fetched successfully")
                .data(tradeService.getBitcoinPrice())
                .build();
    }

    @Operation(summary = "Get All Open Trades")
    @GetMapping("/open")
    public ApiResponse<List<Trade>> getOpenTrades() {

        return ApiResponse.<List<Trade>>builder()
                .success(true)
                .message("Open trades fetched successfully")
                .data(tradeService.getOpenTrades())
                .build();
    }

    @Operation(summary = "Get All Closed Trades")
    @GetMapping("/closed")
    public ApiResponse<List<Trade>> getClosedTrades() {

        return ApiResponse.<List<Trade>>builder()
                .success(true)
                .message("Closed trades fetched successfully")
                .data(tradeService.getClosedTrades())
                .build();
    }
    @Operation(
            summary="Dashboard",
            description="Returns dashboard statistics including live price and total profit."
    )
    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard() {

        DashboardResponse response = tradeService.getDashboard();

        return ApiResponse.<DashboardResponse>builder()
                .success(true)
                .message("Dashboard fetched successfully")
                .data(response)
                .build();
    }

    @Operation(
            summary="Trade History",
            description="Returns paginated trade history."
    )
    @GetMapping("/history")
    public ApiResponse<Page<Trade>> getHistory(

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sort

    ) {

        Page<Trade> trades =
                tradeService.getTradeHistory(page, size, sort);

        return ApiResponse.<Page<Trade>>builder()
                .success(true)
                .message("Trade history fetched successfully")
                .data(trades)
                .build();
    }

    @Operation(
            summary="Search Trades",
            description="Search trades using status and symbol."
    )
    @GetMapping("/search")
    public ApiResponse<List<Trade>> searchTrades(

            @RequestParam(required = false) String status,

            @RequestParam(required = false) String symbol

    ) {

        List<Trade> trades =
                tradeService.searchTrades(status, symbol);

        return ApiResponse.<List<Trade>>builder()
                .success(true)
                .message("Trades fetched successfully")
                .data(trades)
                .build();
    }
    @Operation(
            summary = "Trade Statistics",
            description = "Returns complete trading statistics."
    )
    @GetMapping("/statistics")
    public ApiResponse<TradeStatisticsResponse> getStatistics() {

        return ApiResponse.<TradeStatisticsResponse>builder()
                .success(true)
                .message("Trade statistics fetched successfully")
                .data(tradeService.getTradeStatistics())
                .build();

    }

    @GetMapping("/profit-chart")
    public ResponseEntity<ApiResponse<List<ProfitChartResponse>>> getProfitChart() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profit chart fetched successfully",
                        tradeService.getProfitChart()
                )
        );

    }


    @GetMapping("/telegram-test")
    public String telegramTest() {

        telegramService.sendMessage("🚀 MoneyBot Connected Successfully!");

        return "Telegram Message Sent";

    }

}
