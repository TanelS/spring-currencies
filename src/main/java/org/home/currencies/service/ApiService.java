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

    /**
     * Constructs an instance of {@code ApiService} with the provided API root URL and API key.
     *
     * @param apiUrl the root URL of the currency beacon API
     * @param apiKey the API key used for authenticating requests to the currency beacon API
     */
    public ApiService(@Value("${currencybeacon.api.root}") String apiUrl,
                       @Value("${currencybeacon.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    /**
     * Retrieves a list of available currencies by making a request to the external API.
     *
     * @return an instance of {@link CurrenciesApiResponse} containing the list of available currencies
     *         and their related information.
     */
    public CurrenciesApiResponse getCurrencies() {
        return this.restClient.get().uri("/currencies").retrieve().body(CurrenciesApiResponse.class);
    }


    /**
     * Fetches the latest exchange rates for a specific base currency against a list of target currencies.
     *
     * @param baseCurrency the currency code of the base currency for which exchange rates are to be retrieved
     * @param symbols a list of currency codes representing the target currencies to retrieve exchange rates for
     * @return an instance of {@link RatesApiResponse} containing the exchange rates data, including the base currency,
     *         target currencies, and their respective rates
     */
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

