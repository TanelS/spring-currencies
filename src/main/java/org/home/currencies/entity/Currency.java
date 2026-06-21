package org.home.currencies.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "currency", indexes = {
        @Index(name = "idx_currency_currencyname", columnList = "currencyName"),
        @Index(name = "idx_currency_symbol", columnList = "symbol"),
        @Index(name = "idx_currency_createdate", columnList = "createDate"),
        @Index(name = "idx_currency_modifieddate", columnList = "modifiedDate")
})
@Getter
@Setter
public class Currency extends CommonColumns {

    @Column(nullable = false, columnDefinition = "TEXT", comment = "Currency name")
    private String currencyName;

    @Column(nullable = false, length = 3, unique = true, comment = "Currency ISO 4217 alphabetic code")
    private String currencyCode;

    @Column(nullable = false, length = 3, unique = true, comment = "Currency ISO 4217 numeric code")
    private String currencyNumCode;

    @Column(nullable = false, comment = "Currency decimal precision - number of digits after the decimal separator")
    private int precision;

    @Column(nullable = false, comment = "Currency subunit - smallest unit of currency")
    private int subunit;

    @Column(nullable = false, columnDefinition = "TEXT", comment = "Currency symbol")
    private String symbol;

    @Column(nullable = false, comment = "Currency symbol position - True if symbol comes before the amount")
    private boolean symbolFirst;

    @Column(nullable = false, comment = "Currency decimal separator")
    private char decimalMark;

    @Column(nullable = true, comment = "Currency thousands separator")
    private Character thousandsSeparator;

    @OneToMany(mappedBy = "currency", orphanRemoval = true)
    private Set<Rate> rates = new LinkedHashSet<>();

    @OneToMany(mappedBy = "baseCurrency", orphanRemoval = true)
    private Set<Rate> baseRates = new LinkedHashSet<>();



}
