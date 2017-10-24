package com.elavon.converge.model;

/**
 * Recommended for swiped or Contactless transactions
 * The transaction entry indicator to indicate how the track data was captured.
 */

public enum ElavonEntryMode{
    MANUAL_ENTRY_ONLY("01"),
    KEY_ENTERED_CARD_PRESENT("02"),
    SWIPED("03"), // Default value when track Data is sent alone
    CONTACTLESS("04");

    private final String value;

    ElavonEntryMode(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }
}