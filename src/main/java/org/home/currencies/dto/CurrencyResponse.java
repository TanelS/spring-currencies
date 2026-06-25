package org.home.currencies.dto;
import org.home.currencies.entity.Currency;
import java.util.List;

public record CurrencyResponse(
        Currency base,
        List<Currency> rates
) {}
