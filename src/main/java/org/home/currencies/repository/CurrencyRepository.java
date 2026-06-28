package org.home.currencies.repository;

import org.home.currencies.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCurrencyCode(String currencyCode);

    default Set<String> getAllCurrencyCodes() {
        Set<String> codes = findAll().stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toSet());
        return codes;
    }
}
