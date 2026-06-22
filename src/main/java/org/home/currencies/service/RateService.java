package org.home.currencies.service;

import org.home.currencies.entity.Currency;
import org.home.currencies.entity.Rate;
import org.home.currencies.repository.CurrencyRepository;
import org.home.currencies.repository.RateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

@Service
public class RateService {
    private final CurrencyRepository currencyRepository;
    private final RateRepository rateRepository;

    public record RateImportResult(int imported, int skipped) {
    }

    public RateService(CurrencyRepository currencyRepository, RateRepository rateRepository) {
        this.currencyRepository = currencyRepository;
        this.rateRepository = rateRepository;
    }

    /**
     * Creates and stores exchange rates for a given base currency and rate date.
     * Rates are imported from the provided data map. If a rate already exists
     * for a specific currency on the specified date, it is skipped.
     *
     * @param rateDate        The date for which the rates are being created.
     * @param baseCurrencyStr The ISO 4217 code of the base currency.
     * @param ratesData       A map containing currency codes as keys and their exchange rates as values.
     *                        Rates with a null value will be skipped.
     * @return A {@code RateImportResult} record containing the counts of imported and skipped rates.
     */
    @Transactional
    public RateImportResult createRate(Instant rateDate, String
            baseCurrencyStr, HashMap<String, BigDecimal> ratesData) {
        Currency baseCurrencyEntity = currencyRepository.findByCurrencyCode(baseCurrencyStr).orElseThrow();
        int importedCount = 0;
        int skippedCount = 0;


        for (var entry : ratesData.entrySet()) {
            String currencyCode = entry.getKey();
            BigDecimal rate = entry.getValue();

            if (rate == null) {
                skippedCount++;
                continue;
            }

            Currency currencyEntity = currencyRepository.findByCurrencyCode(currencyCode).orElseThrow();

            if (rateRepository.findByRateDateAndCurrencyAndBaseCurrency(
                    rateDate,
                    currencyEntity,
                    baseCurrencyEntity).isPresent()) {
                skippedCount++;
                continue;
            }

            Rate newRate = new Rate();
            newRate.setCurrency(currencyEntity);
            newRate.setRateDate(rateDate);
            newRate.setBaseCurrency(baseCurrencyEntity);
            newRate.setRate(rate);

            rateRepository.save(newRate);
            importedCount++;
        }
        return new RateImportResult(importedCount, skippedCount);
    }

}

