package com.elavon.converge.util;

import java.math.BigDecimal;
import java.util.Currency;

public class CurrencyUtil {

    /**
     * Converts amount from cents to dollars. Eg. 500L returns 5.00 in BigDecimal for USD
     */
    public static BigDecimal getAmount(final Long amount, final String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        final Currency cur = Currency.getInstance(currency);
        final BigDecimal faction = BigDecimal.TEN.pow(cur.getDefaultFractionDigits());
        return BigDecimal.valueOf(amount).divide(faction);
    }

    /**
     * Converts amount from dollars to cents. Eg. 5.00 in BigDecimal return 500L for USD
     */
    public static Long getAmount(final BigDecimal amount, final String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        final Currency cur = Currency.getInstance(currency);
        final BigDecimal faction = BigDecimal.TEN.pow(cur.getDefaultFractionDigits());
        return amount.multiply(faction).longValue();
    }
}
