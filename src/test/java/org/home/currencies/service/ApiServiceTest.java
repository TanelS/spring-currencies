package org.home.currencies.service;

import org.home.currencies.dto.CurrenciesApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ApiServiceTest {

    @Autowired
    private ApiService service;

    @Test
    void currencyFetch() {
        CurrenciesApiResponse currResponse = service.getCurrencies();
        assertNotNull(currResponse);
    }

}
