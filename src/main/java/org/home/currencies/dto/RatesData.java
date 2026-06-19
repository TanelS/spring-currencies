package org.home.currencies.dto;

import java.time.Instant;
import java.util.HashMap;

public record RatesData(
        Instant date,
        String base,
        HashMap<String, Double> rates
) {
}
