package org.home.currencies.dto;

import java.util.List;

public record CurrenciesApiResponse(List<CurrencyInfo> response) {
}
