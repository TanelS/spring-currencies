package org.home.currencies.exception;

public class CurrencyNotFoundException extends RuntimeException {
    public CurrencyNotFoundException(String currType, String code) {
        super(currType + " " + "'" + code + "'" + " not found");
    }
}
