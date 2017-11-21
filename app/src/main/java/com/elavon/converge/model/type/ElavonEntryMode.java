package com.elavon.converge.model.type;

/**
 * Recommended for swiped or Contactless transactions
 * The transaction entry indicator to indicate how the track data was captured.
 */
public enum ElavonEntryMode {
    MANUAL_ENTRY_ONLY("01"),
    KEY_ENTERED_CARD_PRESENT("02"),
    SWIPED("03"), // Default value when track Data is sent alone
    CONTACTLESS("04"), // for MSD
    EMV_WITH_CVV("05"),
    EMV_PROXIMITY_READ("06"), // for EMV CL
    EMV_WITHOUT_CVV("07");

    private final String value;

    ElavonEntryMode(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
