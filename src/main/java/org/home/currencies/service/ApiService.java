package org.home.currencies.service;


import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.dto.RatesApiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.JsonNode;
import org.home.currencies.util.StringCleaner;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ApiService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    /**
     * Constructs an instance of {@code ApiService} with the provided API root URL and API key.
     *
     * @param apiUrl the root URL of the currency beacon API
     * @param apiKey the API key used for authenticating requests to the currency beacon API
     */
    public ApiService(@Value("${currencybeacon.api.root}") String apiUrl,
                      @Value("${currencybeacon.api.key}") String apiKey,
                      ObjectMapper objectMapper) {


        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.objectMapper = objectMapper;
    }


    /**
     * Retrieves the list of available currencies from the remote API.
     *
     * @return an instance of {@link CurrenciesApiResponse} containing the details of the available currencies,
     * or {@code null} if the request fails or no data is available
     */
    public CurrenciesApiResponse getCurrencies() {
        JsonNode response;
        try {
            response = this.restClient.get().uri("/currencies").retrieve().body(JsonNode.class);
        } catch (Exception e) {
            logger.error("Failed to fetch currencies", e);
            return null;
        }

        if (response == null) {
            return null;
        }
        logger.info("Nothing horrible happened, got some currencies");  //TODO remove later
        JsonNode cleanedTree = StringCleaner.cleanTree(response);
        return objectMapper.treeToValue(cleanedTree, CurrenciesApiResponse.class);
    }


    /**
     * Retrieves the latest exchange rates for a specified base currency and a list of target currencies.
     *
     * @param baseCurrency the base currency for which exchange rates will be fetched
     * @param symbols      a list of target currencies for which the exchange rates are requested
     * @return an instance of {@link RatesApiResponse} containing exchange rate information,
     * or {@code null} if the request fails or no data is available
     */
    public RatesApiResponse getRates(String baseCurrency, List<String> symbols) {

        JsonNode response;
        String symbolString = String.join(",", symbols);
        try {
            response = this.restClient.get().uri(builder -> builder
                            .path("/latest")
                            .queryParam("base", baseCurrency)
                            .queryParam("symbols", symbolString)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);

        } catch (Exception e) {
            logger.error("Failed to fetch currency rates", e);
            return null;
        }

        if (response == null) {
            return null;
        }

        JsonNode cleanedTree = StringCleaner.cleanTree(response);
        return objectMapper.treeToValue(cleanedTree, RatesApiResponse.class);

    }
}

