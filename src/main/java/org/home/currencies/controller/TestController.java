package org.home.currencies.controller;

import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.dto.RatesApiResponse;
import org.home.currencies.service.ApiService;
import org.home.currencies.service.CurrencyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/currency-testing")
public class TestController {
    private final ApiService service;
    private final CurrencyService currencyService;

    public TestController(ApiService service, CurrencyService currencyService) {
        this.service = service;
        this.currencyService = currencyService;
    }

    @GetMapping("/currencies")
    public CurrenciesApiResponse getCurrencies() {
        return service.getCurrencies();
    }

    @GetMapping("/rates")
    public RatesApiResponse getRates(@RequestParam String baseCurrency, @RequestParam List<String> symbols) {
        return service.getRates(baseCurrency, symbols);
    }

    @PostMapping("/import-currencies")
    public void getCurrenciesFromApi() {
        currencyService.importAllCurrencies();
    }

    @PostMapping("/import-rates")
    public void getRatesFromApi() {
        currencyService.createAllRates();
    }

}
