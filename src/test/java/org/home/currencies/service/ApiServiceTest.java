package org.home.currencies.service;

import org.home.currencies.CurrenciesApplication;
import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.dto.CurrencyInfo;
import org.home.currencies.dto.RatesApiResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ApiServiceTest {


    /**
     * Sets up the test environment by loading environment variables from a `.env` file.
     * This method is executed once before all tests in the test class.
     * It ensures that environmental configurations necessary for the application are
     * loaded and made available as system properties.
     */
    @BeforeAll
    static void setUp() {
        CurrenciesApplication.loadEnvFile();
    }

    @Autowired
    private ApiService service;

    @Test
    void currencyFetch() {
        CurrenciesApiResponse currResponse = service.getCurrencies();
        assertNotNull(currResponse);
        assertFalse(currResponse.response().isEmpty());
        Set<String> shorCodes = currResponse.response()
                .stream()
                .map(CurrencyInfo::short_code)
                .collect(Collectors.toSet());
        assertTrue(shorCodes.contains("EUR"));
    }

    @Test
    void currencyRatesFetch() {
        RatesApiResponse ratesResponse = service.getRates("USD", List.of("EUR", "GBP"));
        assertNotNull(ratesResponse);
        assertFalse(ratesResponse.response().rates().isEmpty());
        assertTrue(ratesResponse.response().rates().keySet().containsAll(List.of("EUR", "GBP")));
        assertEquals("USD", ratesResponse.response().base());
    }

}
