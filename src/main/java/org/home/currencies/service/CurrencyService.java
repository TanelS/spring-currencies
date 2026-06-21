package org.home.currencies.service;

import org.home.currencies.dto.CurrencyInfo;
import org.home.currencies.entity.Currency;
import org.home.currencies.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrencyService {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    private final CurrencyRepository repository;

    public CurrencyService(CurrencyRepository repository) {
        this.repository = repository;
    }


    @Transactional
    void create(CurrencyInfo request) {

        Currency currency = new Currency();
        currency.setCurrencyName(request.name());
        currency.setCurrencyCode(request.short_code());
        currency.setCurrencyNumCode(request.code());
        currency.setPrecision(request.precision());
        currency.setSubunit(request.subunit());
        currency.setSymbol(request.symbol());
        currency.setSymbolFirst(request.symbol_first());
        currency.setDecimalMark(request.decimal_mark());
        currency.setThousandsSeparator(request.thousands_separator());

        repository.save(currency);
    }


}
