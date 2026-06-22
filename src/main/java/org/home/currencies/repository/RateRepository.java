package org.home.currencies.repository;

import org.home.currencies.entity.Currency;
import org.home.currencies.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {
    Optional<Rate> findByRateDateAndCurrencyAndBaseCurrency(Instant rateDate, Currency currency, Currency baseCurrency);

}
