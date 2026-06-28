package org.home.currencies.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

/**
 * Represents exchange rates data for a specific base currency and date.
 *
 * This record encapsulates the base currency, the date the rates were retrieved,
 * and a mapping of target currencies to their respective exchange rates.
 * It serves as a lightweight and immutable data structure that is commonly used
 * for transferring exchange rate information between various service layers or
 * for structuring API responses.
 *
 * Instances of RatesData are typically utilized in financial applications for currency
 * conversion, analytics, or reporting, where up-to-date exchange rate data is essential.
 *
 * @param date  the date and time when the exchange rates were retrieved
 * @param base  the base currency against which the exchange rates are provided
 * @param rates a mapping of currency codes to their exchange rates relative to the base currency
 */
public record RatesData(
        Instant date,
        String base,
        HashMap<String, BigDecimal> rates
) {
}
