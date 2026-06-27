package org.home.currencies.service;

import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.dto.CurrencyInfo;
import org.home.currencies.dto.RatesApiResponse;
import org.home.currencies.entity.Currency;
import org.home.currencies.repository.CurrencyRepository;
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
    private final ApiService apiService;
    private final RateService rateService;

    public CurrencyService(
            CurrencyRepository currencyRepository,
            ApiService apiService,
            RateService rateService
    ) {
        this.currencyRepository = currencyRepository;
        this.apiService = apiService;
        this.rateService = rateService;
    }

    /**
     * Retrieves a set of all unique currency codes available in the system.
     *
     * @return a set of strings representing currency codes.
     */
    public Set<String> getAllCurrencyCodes() {
        Set<String> codes = currencyRepository.findAll().stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toSet());
        return codes;
    }


    /**
     * Creates and persists a new currency in the database if it does not already exist.
     * <p>
     * The method checks if a currency with the specified short code already exists in the repository.
     * If it exists, the method returns false without making any changes. Otherwise, it creates a new
     * currency entity with details provided in the request and saves it to the repository.
     * <p>
     * This method is marked as transactional to ensure data consistency.
     *
     * @param request an object containing the details of the currency to be created, such as
     *                name, short code, numeric code, precision, subunit, symbol,
     *                decimal mark, and thousands separator.
     * @return true if the currency was successfully created and persisted, false if it already exists.
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
     * Imports all currencies from an external API and persists them in the database.
     * <p>
     * This method fetches currency data from an external API and attempts to create and save new
     * currency entities in the database. For each currency retrieved:
     * - It increments the "importedCount" if the currency is successfully created.
     * - It increments the "skippedCount" if the currency already exists.
     * - It increments the "failedCount" if errors occur during the process.
     * <p>
     * Any exceptions encountered during the import of individual currencies are logged, and the
     * method continues processing the remaining currencies. A summary of the import process, including
     * counts for successfully imported, skipped, and failed currencies, is logged at the end.
     * <p>
     * This method is annotated with @Transactional to ensure database consistency and rollback on failure.
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
     * Creates and imports exchange rates for all available currencies retrieved from the database.
     * The method fetches exchange rates between currencies using a third-party API service and processes them into the system.
     * Rates for the same base and target currency (e.g., EUR to EUR) are excluded from processing.
     * The process logs the count of successfully imported, skipped, and failed rates.
     * <p>
     * The following steps are executed:
     * 1. Retrieves all currency codes from the `currencyRepository`.
     * 2. Iterates through each currency code as the base currency.
     * 3. Removes the base currency from the list of target currencies to avoid processing self-rates.
     * 4. Invokes the external API to fetch exchange rates for the given base currency and target currencies.
     * 5. Processes the received exchange rate data:
     * - Logs and increments the failure count if the API response is null or contains no rates.
     * - On successful retrieval, it imports, skips, or handles exceptions related to rates processing,
     * while logging corresponding outcomes.
     * 6. Summarizes and logs the operation results, including counts of imported, skipped, and failed rates.
     * <p>
     * Note: This method is not annotated with `@Transactional` due to observed transaction issues.
     */
//    @Transactional  // this causes transaction issues
    public void createAllRates() {

        Set<String> codes = getAllCurrencyCodes();
        logger.info("Starting to import rates for {} currencies", codes.size());
        int importedCount = 0;
        int failedCount = 0;
        int skippedCount = 0;

        for (String baseCurr : codes) {
            Set<String> targets = new HashSet<>(codes);

            // We remove base currency from the target rate currency list because "EUR" to "EUR" rate is almost always 1.0 :-)
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
                RateService.RateImportResult rateImportResult = rateService.createRate(rateDate, baseCurr, rates);
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
