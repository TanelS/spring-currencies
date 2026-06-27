package org.home.currencies.controller;

import org.home.currencies.dto.BaseCurrencyRateDataOutput;
import org.home.currencies.service.CurrencyService;
import org.home.currencies.service.RateService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rates")
public class RateController {
    private final RateService rateService;
    private final CurrencyService currencyService;

    public RateController(RateService rateService, CurrencyService currencyService) {
        this.rateService = rateService;
        this.currencyService = currencyService;
    }

    @GetMapping("/rates")
    public BaseCurrencyRateDataOutput getRates(
            @RequestParam @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate rateDate,
            @RequestParam String currency,
            @RequestParam(required = false) List<String> targetCurrency) {

        return null;  //TODO temporary return
    }
}
