package org.home.currencies.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

/**
 * Represents exchange rate data for a specific base currency.
 *
 * This record encapsulates information about a base currency, the date
 * the exchange rates were retrieved, and a mapping of target currencies
 * to their respective exchange rates relative to the base currency.
 *
 * Instances of this record are commonly used in financial applications
 * for currency conversion, analytics, or displaying exchange rate
 * information in response to API requests.
 *
 * @param baseCurrency the currency code of the base currency against which
 *                     exchange rates are provided
 * @param rateDate     the date and time when the exchange rates were retrieved
 * @param rates        a mapping of target currency codes to their exchange
 *                     rates relative to the base currency
 */
public record BaseCurrencyRateDataOutput(
        String baseCurrency,
        Instant rateDate,
        HashMap<String, BigDecimal> rates
) {
}
