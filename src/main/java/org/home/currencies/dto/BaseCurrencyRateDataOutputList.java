package org.home.currencies.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Optional;

/**
 * Represents a list of exchange rate data for a specific base currency along with unknown currencies.
 *
 * This record encapsulates a collection of {@link BaseCurrencyRateDataOutput} instances
 * and a list of unknown currency codes. The `rates` field contains detailed exchange rate
 * information for various base currencies, while the `unknownCurrencies` field captures
 * currency codes that were not recognized or supported during the retrieval process.
 *
 * Instances of this record are typically used in financial applications where exchange
 * rate data for multiple base currencies is needed, or to provide additional information
 * about unrecognized currencies during exchange rate queries.
 *
 * @param rates             a list of {@link BaseCurrencyRateDataOutput} records, each representing
 *                          exchange rate data for a base currency
 * @param unknownCurrencies a list of strings representing currency codes that were not recognized
 *                          during the exchange rate retrieval process
 */
public record BaseCurrencyRateDataOutputList(
        List<BaseCurrencyRateDataOutput> rates,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> unknownCurrencies
) {}
