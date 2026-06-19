package org.home.currencies.dto;

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
