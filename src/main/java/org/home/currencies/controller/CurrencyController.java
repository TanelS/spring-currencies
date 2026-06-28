package org.home.currencies.controller;

import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrencyController {
    private final CurrencyService currencyService;

    public CurrencyController(
            CurrencyService currencyService
    ) {
        this.currencyService = currencyService;
    }

    @GetMapping("/currencies")
    public CurrenciesApiResponse getCurrencies() {
        return currencyService.findAllCurrencies();
    }
}
