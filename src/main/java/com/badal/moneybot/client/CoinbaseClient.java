package com.badal.moneybot.client;

import com.badal.moneybot.config.CoinbaseConfig;
import com.badal.moneybot.dto.PriceResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CoinbaseClient {

    private final RestTemplate restTemplate;
    private final CoinbaseConfig config;

    public CoinbaseClient(RestTemplate restTemplate,
                          CoinbaseConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public PriceResponse getBitcoinPrice() {

        String url =
                config.getBaseUrl() +
                        "/v2/prices/BTC-USD/spot";

        return restTemplate.getForObject(url, PriceResponse.class);

    }

}
