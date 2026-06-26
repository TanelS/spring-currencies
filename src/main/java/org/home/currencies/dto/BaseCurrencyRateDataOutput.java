package org.home.currencies.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

public record BaseCurrencyRateDataOutput(
        String baseCurrency,
        Instant timestamp,
        HashMap<String, BigDecimal> rates
) {
}
