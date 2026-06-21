package org.home.currencies.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rate", indexes = {
        @Index(name = "idx_rate_rate", columnList = "rate"),
        @Index(name = "idx_rate_ratedate", columnList = "rateDate"),
        @Index(name = "idx_rate_createdate", columnList = "createDate"),
        @Index(name = "idx_rate_modifieddate", columnList = "modifiedDate")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_rate_currency_id", columnNames = {"currency_id", "base_currency_id", "rateDate"})
})
@Getter
@Setter
public class Rate extends CommonColumns {

    @Column(nullable = false,  precision = 20, scale = 10, comment = "Currency rate agains base currency")
    private BigDecimal rate;

    @Column(nullable = false, comment = "Currency rate date agains base currency")
    private Instant rateDate;

    @ManyToOne
    @JoinColumn(name = "currency_id", comment = "Currency ID", nullable = false)
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "base_currency_id", comment = "Base currency ID", nullable = false)
    private Currency baseCurrency;


}
