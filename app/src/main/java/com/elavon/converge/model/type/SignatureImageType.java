package com.elavon.converge.model.type;

public enum SignatureImageType {
    GIF("GIF"),
    TIF("TIF"),
    JPG("JPG"),
    PNG("PNG");

    private final String value;

    SignatureImageType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }
}
