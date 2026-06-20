package org.home.currencies.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "currency", indexes = {
        @Index(name = "idx_currency_currency_name", columnList = "currency_name"),
        @Index(name = "idx_currency_symbol", columnList = "symbol"),
        @Index(name = "idx_currency_createdate", columnList = "createDate"),
        @Index(name = "idx_currency_modifieddate", columnList = "modifiedDate")
})
@Getter
@Setter
public class Currency extends CommonColumns {

    @Column(nullable = false, comment = "Currency name")
    private String currency_name;

    @Column(nullable = false, unique = true, comment = "Currency ISO 4217 alphabetic code")
    private String currency_code;

    @Column(nullable = false, unique = true, comment = "Currency ISO 4217 numeric code")
    private String currency_num_code;

    @Column(nullable = false, comment = "Currency decimal precision - number of digits after the decimal separator")
    private int precision;

    @Column(nullable = false, comment = "Currency subunit - smallest unit of currency")
    private int subunit;

    @Column(nullable = false, comment = "Currency symbol")
    private String symbol;

    @Column(nullable = false, comment = "Currency symbol position - True if symbol comes before the amount")
    private boolean symbol_first;

    @Column(nullable = false, comment = "Currency decimal separator")
    private char decimal_mark;

    @Column(nullable = true, comment = "Currency thousands separator")
    private Character thousands_separator;

    @OneToMany(mappedBy = "currency", orphanRemoval = true)
    private Set<Rate> rates = new LinkedHashSet<>();

    @OneToMany(mappedBy = "base_currency", orphanRemoval = true)
    private Set<Rate> base_rates = new LinkedHashSet<>();



}
