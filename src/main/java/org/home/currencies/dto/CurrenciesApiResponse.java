package org.home.currencies.dto;

import java.util.List;

/**
 * Represents the response for a currencies API request.
 * <p>
 * This record encapsulates a list of {@link CurrencyInfo} objects, where each object contains
 * detailed information about a specific currency, such as its name, code, symbol, subunit details,
 * and other formatting attributes.
 * <p>
 * Instances of this class are typically used as the return type for API endpoints or methods
 * that fetch the list of available currencies from an external service or database.
 * <p>
 * It plays a central role in transferring data between service layers and consumers, ensuring
 * type safety and clarity when working with currency-related information.
 */
public record CurrenciesApiResponse(List<CurrencyInfo> response) {
}
