package org.home.currencies.service;


import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.dto.RatesApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class ApiService {
    private final RestClient restClient;

    public ApiService(@Value("${currencybeacon.api.root}") String apiUrl,
                       @Value("${currencybeacon.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public CurrenciesApiResponse getCurrencies() {
        return this.restClient.get().uri("/currencies").retrieve().body(CurrenciesApiResponse.class);
    }


    public RatesApiResponse getRates(String baseCurrency, List<String> symbols) {
        String symbolString = String.join(",", symbols);
        return this.restClient.get().uri(builder -> builder
                    .path("/latest")
                    .queryParam("base", baseCurrency)
                    .queryParam("symbols", symbolString)
                    .build())
                .retrieve()
                .body(RatesApiResponse.class);

    }
}

