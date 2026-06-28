package org.home.currencies.repository;

import org.home.currencies.entity.Currency;
import org.home.currencies.entity.Rate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {
    Optional<Rate> findByRateDateAndCurrencyAndBaseCurrency(
            Instant rateDate,
            Currency baseCurrency,
            Currency currency
    );

    @Query(value = """
            SELECT DISTINCT ON (c2.currency_code)
                c.currency_code AS base_currency_code,
                c.currency_name AS base_currency_name,
                r.rate AS rate,
                r.rate_date AS rate_date,
                c2.currency_code AS currency_code,
                c2.currency_name AS currency_name
            FROM currency c
                     INNER JOIN rate r ON r.base_currency_id = c.id
                     INNER JOIN currency c2 ON c2.id = r.currency_id
            WHERE c.currency_code = :baseCurrency
              AND c2.currency_code IN (:targetCurrencies)
              AND (r.rate_date AT TIME ZONE :localTimezone)::date = :rateDate :: date
            ORDER BY c2.currency_code,
                     (r.rate_date AT TIME ZONE :localTimezone) DESC;
            """, nativeQuery = true)
    List<RateQueryResult> findRates(
            @Param("baseCurrency") String baseCurrency,
            @Param("targetCurrencies") List<String> targetCurrencies,
            @Param("rateDate") LocalDate rateDate,
            @Param("localTimezone") String localTimezone
    );


    @Query(value = """
                        SELECT DISTINCT ON (r.rate_date::date)
                (r.rate_date AT TIME ZONE :localTimezone)::date
            FROM rate r;
            """, nativeQuery = true)
    List<LocalDate>findDates(@Param("localTimezone") String localTimezone);
}

