package com.elavon.converge.util;

import android.util.Log;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Currency;

public class CurrencyUtil {

    private static final String TAG = CurrencyUtil.class.getSimpleName();

    /**
     * Converts amount from cents to dollars. Eg. 500L returns 5.00 in BigDecimal for USD
     */
    public static BigDecimal getAmount(final Long amount, final String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        final Currency cur = Currency.getInstance(currency);
        final BigDecimal faction = BigDecimal.TEN.pow(cur.getDefaultFractionDigits());
        return BigDecimal.valueOf(amount)
                .divide(faction)
                .setScale(cur.getDefaultFractionDigits(), BigDecimal.ROUND_HALF_UP);
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

    /**
     * Converts amount from dollars to cents. Eg. 1,000.00 in String returns 100000 for USD
     *
     * @param amount
     * @param currency
     * @return
     */
    public static Long getAmount(final String amount, final String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        final Currency cur = Currency.getInstance(currency);
        final BigDecimal faction = BigDecimal.TEN.pow(cur.getDefaultFractionDigits());
        DecimalFormat decimalFormat = new DecimalFormat();
        try {
            Number number = decimalFormat.parse(amount);
            BigDecimal originalAmount = BigDecimal.valueOf(number.doubleValue());
            Long longAmount = originalAmount.multiply(faction).longValue();
            Log.d(TAG, "Amount:" + amount + " originalAmount: " + originalAmount + " long:" + longAmount);
            return longAmount;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
