package org.home.currencies.dto;

import java.util.List;

/**
 * Represents the response for a currencies metadata API request.
 *
 * This record encapsulates a list of {@link CurrencyInfo}, where each {@link CurrencyInfo}
 * represents detailed metadata about an individual currency. The metadata includes various
 * attributes of each currency, such as its name, ISO codes, precision, and formatting
 * details (e.g., symbol, decimal mark, and thousand separator).
 *
 * Instances of this class are typically used as the return type for API endpoints or service
 * methods that fetch information about multiple currencies. It provides a clean and immutable
 * data structure for transferring currency metadata between service layers or external clients.
 *
 * @param response a list of {@link CurrencyInfo} records, each containing metadata for an individual currency
 */
public record CurrenciesApiResponse(List<CurrencyInfo> response) {
}
