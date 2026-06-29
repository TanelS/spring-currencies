package org.home.currencies.service;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.home.currencies.dto.BaseCurrencyRateDataOutput;
import org.home.currencies.dto.BaseCurrencyRateDataOutputList;
import org.home.currencies.entity.Currency;
import org.home.currencies.entity.Rate;
import org.home.currencies.exception.CurrencyNotFoundException;
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
     * Creates and imports exchange rates for the specified base currency and date.
     *
     * @param rateDate The date for the exchange rates being created.
     * @param baseCurrencyStr The code of the base currency for the exchange rates.
     * @param ratesData A map of currency codes to their corresponding exchange rates.
     *                  The keys represent target currencies, and the values represent the exchange rates.
     * @return A {@code RateImportResult} object containing the number of successfully imported exchange rates
     *         and the number of rates that were skipped due to issues such as missing data or duplicates.
     * @throws CurrencyNotFoundException If the base currency or any provided target currency is not found.
     */
    public RateImportResult createRate(Instant rateDate, String baseCurrencyStr, HashMap<String, BigDecimal> ratesData) {
        Currency baseCurrencyEntity = currencyRepository
                .findByCurrencyCode(baseCurrencyStr)
                .orElseThrow(() -> new CurrencyNotFoundException("Base currency", baseCurrencyStr));
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

                Currency currencyEntity = currencyRepository.
                        findByCurrencyCode(currencyCode)
                        .orElseThrow(() -> new CurrencyNotFoundException("Currency", currencyCode));

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
     * Retrieves exchange rate data for the specified base currency and target currencies on a given date.
     *
     * This method fetches exchange rates from the repository for a base currency and a list of target
     * currencies. If no target currencies are provided, it retrieves rates for all available currencies.
     * The results are returned as a list containing the exchange rate data for each date and target currency.
     *
     * @param baseCurrency    The base currency code for which exchange rates are to be retrieved.
     * @param rateDate        The date for which exchange rates are to be retrieved. If rates are not
     *                        available for this date, the latest available dates will be used.
     * @param targetCurrency  A list of currency codes representing the target currencies. If null or empty,
     *                        rates for all available currencies will be retrieved.
     * @return A {@code BaseCurrencyRateDataOutputList} object containing exchange rate data for the
     *         specified base currency, the target currencies, and the specified rate date. If some target
     *         currencies are unavailable in the repository, they will be listed separately in the output.
     * @throws CurrencyNotFoundException If the specified base currency does not exist in the repository.
     */
    public BaseCurrencyRateDataOutputList getRatesforCurrencies(
            String baseCurrency,
            LocalDate rateDate,
            List<String> targetCurrency) {

        String baseCurrCleaned = StringCleaner.cleanString(baseCurrency).toUpperCase();

        if (currencyRepository.findByCurrencyCode(baseCurrCleaned).isEmpty()) {
            throw new CurrencyNotFoundException("Base currency", baseCurrCleaned);
        }

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

        Set<String> targetCurrenciesUnverified = new HashSet<>(targetList);
        targetCurrenciesUnverified.removeAll(codes);  // currency symbols not in the DB

        List<LocalDate> rateDatesToLoop = (rateDates.contains(rateDate)) ? List.of(rateDate) : rateDates;

        for (LocalDate d : rateDatesToLoop) {
            HashMap<String, BigDecimal> rates = new HashMap<>();
            List<RateQueryResult> queryResult = rateRepository.findRates(baseCurrCleaned, targetList, d, localTimezone);

            if (queryResult.isEmpty() && (targetCurrency != null) && targetCurrency.contains(baseCurrency)) {
                rates.put(baseCurrency, new BigDecimal("1.00"));
                ratesResultList.add(new BaseCurrencyRateDataOutput(baseCurrency, Instant.now(), rates));
                return new BaseCurrencyRateDataOutputList(ratesResultList, null);
            } else if (queryResult.isEmpty()) {
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
        return new BaseCurrencyRateDataOutputList(
                ratesResultList,
                (!targetCurrenciesUnverified.isEmpty()) ? new ArrayList<>(targetCurrenciesUnverified) : null);
    }

}
