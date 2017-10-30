package com.elavon.converge.model.type;

public enum ElavonPosMode {
    MANUAL_ENTRY_ONLY("01"),
    SWIPE_CAPABLE("02"), // Magnetically Swipe Capability
    CL_CAPABLE("03"),    // Proximity / Contactless capable device
    CT_ONLY("04"),       // EMV Chip Capability (ICC) - Contact Only (w/Magstripe)
    CT_MAGSTRIPE("05");  // EMV Chip Capability (ICC) - Dual Interface (w/Magstripe)

    private final String value;

    ElavonPosMode(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
