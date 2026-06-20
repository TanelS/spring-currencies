package org.home.currencies.service;

import org.home.currencies.CurrenciesApplication;
import org.home.currencies.dto.CurrenciesApiResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    }

}
