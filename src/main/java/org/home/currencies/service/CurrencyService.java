package org.home.currencies.service;

import org.home.currencies.dto.CurrenciesApiResponse;
import org.home.currencies.dto.CurrencyInfo;
import org.home.currencies.entity.Currency;
import org.home.currencies.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private final CurrencyRepository repository;
    private final ApiService apiService;

    public CurrencyService(CurrencyRepository repository, ApiService apiService) {
        this.repository = repository;
        this.apiService = apiService;
    }


    /**
     * Creates a new currency and saves it to the database if it does not already exist.
     * <p>
     * This method ensures that a currency with the same short code is not duplicated in the database.
     * If a currency with the provided short code is already present, the method returns false and does not perform any insert operation.
     * Otherwise, the currency is created and saved in the database, and the method returns true.
     *
     * @param request a {@link CurrencyInfo} object containing detailed information about the currency to be created,
     *                such as its name, short code, numeric code, precision, subunit, symbol, and formatting rules.
     * @return {@code true} if the currency was successfully created and saved; {@code false} if the currency already exists.
     */
    @Transactional
    public boolean createCurrency(CurrencyInfo request) {

        if (repository.findByCurrencyCode(request.short_code()).isPresent()) {
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

        repository.save(currency);
        return true;
    }

    /**
     * Imports all available currencies from an external API and saves them into the database.
     * <p>
     * This method:
     * - Retrieves a list of currencies from the external API using {@link ApiService#getCurrencies}.
     * - Attempts to create or save each currency using {@link #createCurrency(CurrencyInfo)}.
     * - Tracks the number of currencies successfully imported, skipped due to being duplicates, or failed due to errors.
     * - Logs the summary of the operation including counts of imported, skipped, and failed currencies.
     * <p>
     * The operation is transactional, ensuring that the database remains consistent in case of an error.
     * <p>
     * Exceptions during the import process for individual currencies are caught and logged,
     * allowing the process to continue with the remaining currencies.
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
}
