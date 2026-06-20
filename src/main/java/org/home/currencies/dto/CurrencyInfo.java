package org.home.currencies.dto;

/**
 * Represents detailed information about a specific currency.
 * <p>
 * This record provides metadata and formatting rules for a currency, enabling consistent handling
 * across various applications, such as financial systems or currency conversion tools.
 */
public record CurrencyInfo(
        int id,
        String name,
        String short_code,
        String code,
        int precision,
        int subunit,
        String symbol,
        boolean symbol_first,
        String decimal_mark,
        String thousands_separator

) {}
