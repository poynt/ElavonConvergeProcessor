package com.elavon.converge.model.type;

/**
 * Created by dennis on 10/24/17.
 */

public enum CardType {
    CASH("CASH"),
    CREDIT("CREDITCARD"),
    DEBIT("DEBITCARD"),
    FOODSTAMP("FOODSTAMP"),
    GIFTCARD("GIFTCARD"),
    LOYALTY("LOYALTY");

    private final String value;

    CardType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }
}