package com.library.bookreturn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/** library.penalty.per-book-per-day: late fee charged per book for each day past the due date. */
@ConfigurationProperties(prefix = "library.penalty")
public record PenaltyProperties(BigDecimal perBookPerDay) {

    public PenaltyProperties {
        if (perBookPerDay == null) {
            perBookPerDay = BigDecimal.ONE;
        }
        if (perBookPerDay.signum() < 0) {
            throw new IllegalArgumentException("library.penalty.per-book-per-day must not be negative");
        }
    }
}
