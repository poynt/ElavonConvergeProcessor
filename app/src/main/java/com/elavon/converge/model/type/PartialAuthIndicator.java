package com.elavon.converge.model.type;

public enum PartialAuthIndicator {
    SUPPORTED(1),
    NOT_SUPPORTED(0);

    private final int value;

    PartialAuthIndicator(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.valueOf(value).toString();
    }
}
