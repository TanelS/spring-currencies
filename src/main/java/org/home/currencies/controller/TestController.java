package org.home.currencies.controller;

import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.service.ApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/currency-testing")
public class TestController {
    private final ApiService service;

    public TestController(ApiService service) {
        this.service = service;
    }

    @GetMapping("/currencies")
    public CurrenciesApiResponse getCurrencies() {
        return service.getCurrencies();
    }
}
