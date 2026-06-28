package org.home.currencies.service;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.home.currencies.dto.BaseCurrencyRateDataOutput;
import org.home.currencies.dto.BaseCurrencyRateDataOutputList;
import org.home.currencies.entity.Currency;
import org.home.currencies.entity.Rate;
import org.home.currencies.repository.CurrencyRepository;
import org.home.currencies.repository.RateQueryResult;
import org.home.currencies.repository.RateRepository;
import org.home.currencies.util.StringCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class RateService {
    private static final Logger logger = LoggerFactory.getLogger(RateService.class);
    private final CurrencyRepository currencyRepository;
    private final RateRepository rateRepository;
    private final SessionFactory sessionFactory;

    @Value("${local.timezone}")
    private String localTimezone;

    public record RateImportResult(int imported, int skipped) {
    }

    public RateService(
            CurrencyRepository currencyRepository,
            RateRepository rateRepository,
            EntityManagerFactory entityManagerFactory) {
        this.currencyRepository = currencyRepository;
        this.rateRepository = rateRepository;
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    }

    /**
     * Creates and imports exchange rates for a given date, base currency, and a map of target currencies with their rates.
     * If a rate already exists for the given combination of date, base currency, and target currency, it is skipped.
     *
     * @param rateDate        The date for which the rates are being created.
     * @param baseCurrencyStr The currency code of the base currency (e.g., "USD").
     * @param ratesData       A map where the key is the target currency code (e.g., "EUR") and the value is the exchange rate.
     * @return A {@code RateImportResult} containing the count of successfully imported rates and skipped rates.
     */
    public RateImportResult createRate(Instant rateDate, String baseCurrencyStr, HashMap<String, BigDecimal> ratesData) {
        Currency baseCurrencyEntity = currencyRepository.findByCurrencyCode(baseCurrencyStr).orElseThrow();
        int importedCount = 0;
        int skippedCount = 0;

        try (StatelessSession session = sessionFactory.openStatelessSession()) {
            Transaction tx = session.beginTransaction();

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

                session.insert(newRate);
                importedCount++;
            }

            tx.commit();
        } catch (Exception e) {
            logger.error("Error inserting rates for base currency {}", baseCurrencyStr, e);
        }

        return new RateImportResult(importedCount, skippedCount);
    }


    /**
     * Retrieves exchange rates for the provided base currency and a list of target currencies
     * for a specific date or a range of available dates.
     *
     * @param baseCurrency the base currency code for which to retrieve exchange rates.
     *                     It should be a non-null string and will be normalized to uppercase.
     * @param rateDate     an optional date for which the exchange rates are required.
     *                     If null or if the date is not available, rates for all available dates are considered.
     * @param targetCurrency a list of target currency codes for which exchange rates are to be fetched.
     *                       If null or empty, rates for all available currencies (excluding the base currency) are returned.
     * @return a {@code BaseCurrencyRateDataOutputList} object containing the exchange rate data for the requested
     *         base currency, target currencies, and the applicable date(s). If no rates are found, the list
     *         may be empty or contain default data.
     */
    public BaseCurrencyRateDataOutputList getRatesforCurrencies(
            String baseCurrency,
            LocalDate rateDate,
            List<String> targetCurrency) {

        String baseCurrCleaned = StringCleaner.cleanString(baseCurrency).toUpperCase();
        List<BaseCurrencyRateDataOutput> ratesResultList = new ArrayList<>();


        Set<String> codes = currencyRepository.getAllCurrencyCodes();
        List<LocalDate> rateDates = rateRepository.findDates(localTimezone);

        codes.removeAll(Set.of(baseCurrCleaned)); // remove the base currency from the target currencies
        List<String> targetList = (targetCurrency == null || targetCurrency.isEmpty()) ? List.copyOf(codes) : targetCurrency;

        targetList = targetList.stream()
                .map(StringCleaner::cleanString)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .toList();

        List<LocalDate> rateDatesToLoop = (rateDates.contains(rateDate)) ? List.of(rateDate) : rateDates;

        for (LocalDate d : rateDatesToLoop) {
            HashMap<String, BigDecimal> rates = new HashMap<>();
            List<RateQueryResult> queryResult = rateRepository.findRates(baseCurrCleaned, targetList, d, localTimezone);

            if (queryResult.isEmpty() && (targetCurrency != null) && targetCurrency.contains(baseCurrency)) {
                rates.put(baseCurrency, new BigDecimal("1.00"));
                ratesResultList.add(new BaseCurrencyRateDataOutput(baseCurrency, Instant.now(), rates));
                return new BaseCurrencyRateDataOutputList(ratesResultList);
            } else if (queryResult.isEmpty()) {
                ratesResultList.add(new BaseCurrencyRateDataOutput(baseCurrency, Instant.now(), rates));
                continue;
            }

            String base = queryResult.getFirst().getBaseCurrencyCode();
            Instant date = queryResult.getFirst().getRateDate();

            for (RateQueryResult r : queryResult) {
                String currCode = r.getCurrencyCode();
                BigDecimal currRate = r.getRate();
                rates.put(currCode, currRate);
            }
            ratesResultList.add(new BaseCurrencyRateDataOutput(base, date, rates));
        }
        return new BaseCurrencyRateDataOutputList(ratesResultList);
    }

}
