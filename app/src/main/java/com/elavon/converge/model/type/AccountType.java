package com.elavon.converge.model.type;

public enum AccountType {
    CHECKING("0"),
    SAVING("1");

    private final String value;

    AccountType(final String value) {
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
