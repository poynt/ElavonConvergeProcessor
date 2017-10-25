package com.elavon.converge.model;

public enum PartialAuthIndicator {
    SUPPORTED(1);

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
