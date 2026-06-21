package org.home.currencies.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

/**
 * Represents exchange rate data for a specific base currency.
 * <p>
 * This record provides the date the rates were retrieved, the base currency,
 * and a mapping of other currencies to their respective exchange rates relative to the base currency.
 * It is designed to encapsulate essential information for currency conversion or financial computations.
 */
public record RatesData(
        Instant date,
        String base,
        HashMap<String, BigDecimal> rates
) {
}
