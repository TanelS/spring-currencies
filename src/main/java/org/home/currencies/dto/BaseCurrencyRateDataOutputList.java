package org.home.currencies.dto;

import java.util.List;

public record BaseCurrencyRateDataOutputList(
        List<BaseCurrencyRateDataOutput> rates
) {}
