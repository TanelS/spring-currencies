package org.home.currencies.service;


import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.dto.RatesApiResponse;
import org.home.currencies.util.StringCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Service
public class ApiService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    /**
     * Constructs an instance of {@code ApiService}.
     *
     * @param apiUrl the base URL of the CurrencyBeacon API for making requests
     * @param apiKey the API key used for authentication with the CurrencyBeacon API
     * @param objectMapper the {@link ObjectMapper} used for processing JSON responses
     */
    public ApiService(@Value("${currencybeacon.api.root}") String apiUrl,
                      @Value("${currencybeacon.api.key}") String apiKey,
                      ObjectMapper objectMapper) {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(50));

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestFactory(requestFactory)
                .build();
        this.objectMapper = objectMapper;
    }


    /**
     * Fetches the list of available currencies by invoking the currencies API endpoint.
     *
     * This method performs an HTTP GET request to retrieve currency data, processes the JSON response,
     * cleans any string values within the JSON structure using {@link StringCleaner#cleanTree(JsonNode)},
     * and maps the cleaned data to an instance of {@link CurrenciesApiResponse}.
     *
     * In the event of an error or if the response is null, the method logs the issue and returns null.
     *
     * @return an instance of {@link CurrenciesApiResponse} representing the list of available currencies,
     * or {@code null} if the request fails or the response is empty
     */
    public CurrenciesApiResponse getCurrencies() {
        try {
            JsonNode response = this.restClient.get().uri("/currencies").retrieve().body(JsonNode.class);

            if (response == null) {
                return null;
            }

            JsonNode cleanedTree = StringCleaner.cleanTree(response);
            return objectMapper.treeToValue(cleanedTree, CurrenciesApiResponse.class);
        } catch (Exception e) {
            logger.error("Failed to fetch currencies", e);
            return null;
        }
    }


    /**
     * Fetches exchange rates for a given base currency and a list of target currencies.
     *
     * This method performs an HTTP GET request to the exchange rates API, specifying the base currency
     * and the target currencies as query parameters. It retrieves JSON data, cleans up string values
     * using {@link StringCleaner#cleanTree(JsonNode)}, and maps the cleaned response data to an
     * instance of {@link RatesApiResponse}.
     *
     * If an error occurs during the request, or if the response is null, the method logs the error
     * and returns {@code null}.
     *
     * @param baseCurrency the base currency for which exchange rates should be retrieved
     *                     (e.g., "USD" or "EUR")
     * @param symbols      a list of target currency codes for which exchange rates should be fetched
     *                     (e.g., ["GBP", "JPY", "AUD"])
     * @return an instance of {@link RatesApiResponse} containing the base currency, exchange rates,
     *         and other relevant data, or {@code null} if the request fails or the response is empty
     */
    public RatesApiResponse getRates(String baseCurrency, List<String> symbols) {
        String symbolString = String.join(",", symbols);
        try {
            JsonNode response = this.restClient.get().uri(builder -> builder
                            .path("/latest")
                            .queryParam("base", baseCurrency)
                            .queryParam("symbols", symbolString)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                return null;
            }

            JsonNode cleanedTree = StringCleaner.cleanTree(response);
            return objectMapper.treeToValue(cleanedTree, RatesApiResponse.class);
        } catch (Exception e) {
            logger.error("Failed to fetch currency rates", e);
            return null;
        }
    }
}

