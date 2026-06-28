package org.home.currencies.dto;
import org.home.currencies.entity.Currency;

/**
 * Represents detailed information about a currency.
 *
 * Instances of this record encapsulate various attributes of a currency,
 * including its name, ISO codes, precision, subunit, symbol, and formatting
 * information, such as the decimal and thousands separators.
 *
 * This record is commonly used to transfer currency-related data between
 * service layers or to structure responses for API requests involving
 * currency metadata.
 *
 * @param id                the unique identifier of the currency
 * @param name              the name of the currency
 * @param short_code        the short code representing the currency
 * @param code              the numeric or alphabetic code of the currency
 * @param precision         the number of decimal places for currency amounts
 * @param subunit           the smallest monetary subunit of the currency
 * @param symbol            the symbol representing the currency (e.g., $)
 * @param symbol_first      the positioning of the symbol relative to the amount
 * @param decimal_mark      the character used as the decimal separator
 * @param thousands_separator the character used as the thousands separator
 */
public record CurrencyInfo(
        Long id,
        String name,
        String short_code,
        String code,
        int precision,
        int subunit,
        String symbol,
        boolean symbol_first,
        Character decimal_mark,
        Character thousands_separator

) {
    /**
     * Converts a {@link Currency} object into a {@link CurrencyInfo} record.
     * This method extracts relevant fields from the given {@link Currency} object
     * and maps them to a corresponding {@link CurrencyInfo} instance.
     *
     * @param currency the {@link Currency} object to be converted
     * @return a {@link CurrencyInfo} instance containing details extracted from the given {@link Currency} object
     */
    public static CurrencyInfo from (Currency currency) {
        return new CurrencyInfo(
                currency.getId(),
                currency.getCurrencyName(),
                currency.getCurrencyCode(),
                currency.getCurrencyNumCode(),
                currency.getPrecision(),
                currency.getSubunit(),
                currency.getSymbol(),
                currency.isSymbolFirst(),
                currency.getDecimalMark(),
                currency.getThousandsSeparator()
        );
    }

}
