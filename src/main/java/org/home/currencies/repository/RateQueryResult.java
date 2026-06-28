package org.home.currencies.repository;

import java.math.BigDecimal;
import java.time.Instant;

public interface RateQueryResult {
    String getBaseCurrencyCode();
    BigDecimal getRate();
    Instant getRateDate();
    String getCurrencyCode();
    String getCurrencyName();
    String getBaseCurrencyName();

}
