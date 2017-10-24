package com.elavon.converge.model;

/**
 * Created by dennis on 10/23/17.
 */

public enum PartialAuthIndicator {
    SUPPORTED(1);

    private final int value;

    PartialAuthIndicator(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return ""+value;
    }
}
