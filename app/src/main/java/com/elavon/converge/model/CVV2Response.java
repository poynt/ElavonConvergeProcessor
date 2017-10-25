package com.elavon.converge.model;

/**
 * Created by dennis on 10/24/17.
 */

public enum CVV2Response {
    MATCH("M"), // CVV2/CVC2 Match
    NO_MATCH("N"), // CVV2/CVC2 No match
    NOT_PROCESSED("P"), // Not processed
    CVV_EXPECTED("S"), // Issuer indicates that the CVV2/CVC2 data should be present on the card, but the merchant
            // has indicated that the CVV2/CVC2 data is not present on the card

    NOT_AVAILABLE("U"); // Issuer has not certified for CVV2/CVC2 or Issuer has not provided Visa with the CVV2/CVC2 encryption keys

    private final String value;

    CVV2Response(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }
}
