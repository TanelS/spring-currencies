package org.home.currencies.service;

import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.dto.CurrencyInfo;
import org.home.currencies.dto.RatesApiResponse;
import org.home.currencies.entity.Currency;
import org.home.currencies.entity.Rate;
import org.home.currencies.repository.CurrencyRepository;
import org.home.currencies.repository.RateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private final CurrencyRepository currencyRepository;
    private final RateRepository rateRepository;
    private final ApiService apiService;

    public record RateImportResult(int imported, int skipped) {
    }

    public CurrencyService(
            CurrencyRepository currencyRepository,
            RateRepository rateRepository,
            ApiService apiService
    ) {
        this.currencyRepository = currencyRepository;
        this.rateRepository = rateRepository;
        this.apiService = apiService;
    }


    /**
     * Creates a new currency entry in the database based on the provided currency information.
     * If a currency with the same currency code already exists in the database, no new entry
     * will be created, and the method will return false.
     *
     * @param request the {@link CurrencyInfo} object containing details of the currency to be created
     *                such as currency name, code, symbol, subunit, and formatting details.
     * @return {@code true} if the currency was successfully created and saved into the database;
     * {@code false} if a currency with the given code already exists.
     */
    @Transactional
    public boolean createCurrency(CurrencyInfo request) {

        if (currencyRepository.findByCurrencyCode(request.short_code()).isPresent()) {
            return false;
        }

        Currency currency = new Currency();
        currency.setCurrencyName(request.name());
        currency.setCurrencyCode(request.short_code());
        currency.setCurrencyNumCode(request.code());
        currency.setPrecision(request.precision());
        currency.setSubunit(request.subunit());
        currency.setSymbol(request.symbol());
        currency.setSymbolFirst(request.symbol_first());
        currency.setDecimalMark(request.decimal_mark());
        currency.setThousandsSeparator(request.thousands_separator());

        currencyRepository.save(currency);
        return true;
    }

    /**
     * Imports all available currencies from an external API and persists them in the database.
     * Each currency import operation is evaluated and categorized as either successful, skipped,
     * or failed. A summary of the result is logged at the end of the process.
     * <p>
     * The method retrieves the currency information via the {@code apiService.getCurrencies()} call.
     * For each currency returned, the method attempts to create a new currency entry in the database
     * using the {@code createCurrency} method. If a currency already exists in the database,
     * it is skipped. If an error occurs during the import of a specific currency, it is counted and logged.
     * <p>
     * This method is transactional, ensuring that database operations are committed or rolled back
     * appropriately based on the success or failure of the transactions.
     * <p>
     * Logs:
     * - Errors encountered during individual currency imports.
     * - Summary of the import process, including counts of successfully imported, skipped, and failed currencies.
     */
    @Transactional
    public void importAllCurrencies() {
        CurrenciesApiResponse response = apiService.getCurrencies();
        int importedCount = 0;
        int failedCount = 0;
        int skippedCount = 0;

        for (CurrencyInfo curr : response.response()) {
            try {
                boolean importSuccess = createCurrency(curr);

                if (importSuccess) {
                    importedCount++;
                } else {
                    skippedCount++;
                }

            } catch (Exception e) {
                failedCount++;
                logger.error("Error importing currencies - failing cuurrency: {}", curr.name(), e);
            }
        }
        logger.info("Imported: {} currencies, skipped:{}, failed: {}", importedCount, skippedCount, failedCount);
    }


    /**
     * Creates and imports exchange rates for the specified rate date, base currency,
     * and a set of currency rates.
     * <p>
     * The method saves valid rates into the database while skipping entries that are
     * invalid, null, or already exist in the repository. The result of the operation
     * is returned as a summary of the number of successfully imported and skipped rates.
     *
     * @param rateDate        The date for which the rates are being imported.
     * @param baseCurrencyStr The code of the base currency for the rates.
     * @param ratesData       A map containing currency codes as keys and their exchange rates as values.
     *                        Null values in the map will result in the rates being skipped.
     * @return A {@link RateImportResult} containing the count of successfully imported rates
     * and the count of skipped rates.
     */
    @Transactional
    public RateImportResult createRate(Instant rateDate, String baseCurrencyStr, HashMap<String, BigDecimal> ratesData) {
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

    /**
     * Imports and processes exchange rates for all currency codes retrieved from the currency repository.
     * <p>
     * This method fetches a set of all available currency codes and iterates over each code
     * to process exchange rates relative to other currency codes. For each base currency,
     * it retrieves exchange rate data from an external API service and attempts to import
     * the rates into the system. The results of the process, including the count of imported,
     * skipped, and failed rates, are logged.
     * <p>
     * Key operations in this method:
     * - Retrieves all currency codes from the currency repository.
     * - For each base currency, fetches rates against all other currencies using an API service.
     * - Logs and skips processing for cases where rates cannot be retrieved or are empty.
     * - Invokes a helper method, {@code createRate}, to import the fetched rates.
     * - Handles and logs any exceptions occurring during the rate import process.
     * <p>
     * Statistics including the number of imported, skipped, and failed rates are logged
     * at the end of the method's execution.
     * <p>
     * This method is annotated with {@code @Transactional} to ensure the process is completed
     * atomically and database changes are rolled back in case of errors.
     */
    @Transactional
    public void createAllRates() {

        Set<String> codes = currencyRepository.findAll().stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toSet());

//        Set<String> codes = Set.of("USD", "EUR", "GBP");  //TODO delete - for testing

        int importedCount = 0;
        int failedCount = 0;
        int skippedCount = 0;

        for (String baseCurr : codes) {
            Set<String> targets = new HashSet<>(codes);
            targets.removeAll(Set.of(baseCurr));
            List<String> targetList = new ArrayList<>(targets);

            RatesApiResponse ratesResponse = apiService.getRates(baseCurr, targetList);

            if (ratesResponse == null) {
                failedCount += targetList.size();
                logger.error("Base currency {} does return any data", baseCurr);
                continue;
            }

            Instant rateDate = ratesResponse.response().date();
            HashMap<String, BigDecimal> rates = ratesResponse.response().rates();

            try {
                if (rates.isEmpty()) {
                    failedCount += targetList.size();
                    logger.error("Base currency {} does not have rates", baseCurr);
                    continue;
                }
                logger.info("Working on base {} currency rates", baseCurr);
                RateImportResult rateImportResult = createRate(rateDate, baseCurr, rates);
                importedCount += rateImportResult.imported();
                skippedCount += rateImportResult.skipped();


            } catch (Exception e) {
                failedCount++;
                logger.error("Error importing rates - failing base rate: {}", baseCurr, e);
            }
        }
        logger.info("Imported: {} rates, skipped:{}, failed: {}", importedCount, skippedCount, failedCount);
    }

}
