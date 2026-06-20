package org.home.currencies.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "currency", indexes = {
        @Index(name = "idx_currency_currency_name", columnList = "currency_name"),
        @Index(name = "idx_currency_symbol", columnList = "symbol"),
        @Index(name = "idx_currency_createdate", columnList = "createDate"),
        @Index(name = "idx_currency_modifieddate", columnList = "modifiedDate")
})
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


//    @OneToMany(mappedBy = "currency", fetch = FetchType.LAZY)  //TODO work on it whenb rates entity is done
//    private List<Rate> rates = new ArrayList<>();


    public String getCurrency_name() {
        return currency_name;
    }

    public void setCurrency_name(String currency_name) {
        this.currency_name = currency_name;
    }

    public String getCurrency_code() {
        return currency_code;
    }

    public void setCurrency_code(String currency_code) {
        this.currency_code = currency_code;
    }

    public String getCurrency_num_code() {
        return currency_num_code;
    }

    public void setCurrency_num_code(String currency_num_code) {
        this.currency_num_code = currency_num_code;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getSubunit() {
        return subunit;
    }

    public void setSubunit(int subunit) {
        this.subunit = subunit;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isSymbol_first() {
        return symbol_first;
    }

    public void setSymbol_first(boolean symbol_first) {
        this.symbol_first = symbol_first;
    }

    public char getDecimal_mark() {
        return decimal_mark;
    }

    public void setDecimal_mark(char decimal_mark) {
        this.decimal_mark = decimal_mark;
    }

    public Character getThousands_separator() {
        return thousands_separator;
    }

    public void setThousands_separator(Character thousands_separator) {
        this.thousands_separator = thousands_separator;
    }
}
