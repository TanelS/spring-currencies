package org.home.currencies.dto;

/**
 * Represents the response for an exchange rates API request.
 *
 * This record encapsulates an instance of {@link RatesData}, which contains
 * exchange rate information for a specific base currency and date. The exchange
 * rate data includes the base currency, the date the rates were retrieved, and
 * a mapping of target currencies to their respective exchange rates.
 *
 * Instances of this class are typically used as the return type for API endpoints
 * or service methods that fetch exchange rate information from an external service
 * or database. It provides a clean and immutable data structure to transfer rates
 * data between service layers or to users.
 *
 * @param response the exchange rates data, represented as a {@link RatesData} record
 */
public record RatesApiResponse(RatesData response) {
}
