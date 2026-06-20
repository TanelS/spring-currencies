package org.home.currencies.dto;

/**
 * Represents the response for an exchange rates API request.
 * <p>
 * This record encapsulates exchange rates data, including the base currency, the date the rates
 * were retrieved, and a mapping of target currencies to their respective exchange rates against
 * the base currency.
 * <p>
 * Instances of this class are typically used as the return type for API endpoints or methods
 * that fetch exchange rate information from an external service or database.
 * <p>
 * It plays a central role in transferring exchange rate data between service layers and consumers,
 * ensuring type safety and clarity when working with financial applications that rely on currency
 * conversion.
 *
 * @param response an instance of {@link RatesData} containing the base currency, date,
 *                 and exchange rates mapping
 */
public record RatesApiResponse(RatesData response) {
}
