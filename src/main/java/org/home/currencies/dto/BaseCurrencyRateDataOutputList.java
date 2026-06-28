package org.home.currencies.dto;

import java.util.List;

/**
 * Represents a collection of exchange rate data for various base currencies.
 *
 * This record encapsulates a list of {@link BaseCurrencyRateDataOutput} instances, where each
 * {@link BaseCurrencyRateDataOutput} contains exchange rate information for a specific base currency,
 * including the base currency code, the date the rates were retrieved, and a mapping of target currencies
 * to their respective exchange rates.
 *
 * Instances of this class are commonly used as the return type for API endpoints or service methods that
 * fetch exchange rate information for multiple base currencies. It provides a structured and immutable
 * representation of exchange rate data.
 *
 * @param rates a list of {@link BaseCurrencyRateDataOutput} objects, where each object contains detailed
 *              exchange rate information for a specific base currency
 */
public record BaseCurrencyRateDataOutputList(
        List<BaseCurrencyRateDataOutput> rates
) {}
