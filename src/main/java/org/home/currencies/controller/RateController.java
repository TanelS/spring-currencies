package org.home.currencies.controller;

import jakarta.validation.constraints.PastOrPresent;
import org.home.currencies.dto.BaseCurrencyRateDataOutputList;
import org.home.currencies.service.RateService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
public class RateController {
    private final RateService rateService;

    public RateController(RateService rateService) {
        this.rateService = rateService;
    }

    @GetMapping("/rates")
    public BaseCurrencyRateDataOutputList getRates(
            @RequestParam (required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") @PastOrPresent LocalDate rateDate,
            @RequestParam String baseCurrency,
            @RequestParam (required = false) List<String> targetCurrency) {

        return rateService.getRatesforCurrencies(baseCurrency, rateDate, targetCurrency);
    }
}
