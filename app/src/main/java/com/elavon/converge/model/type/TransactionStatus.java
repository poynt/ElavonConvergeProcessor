package com.elavon.converge.model.type;

public enum TransactionStatus {
    PEN("Pended"),
    OPN("Open"),
    REV("Review"),
    STL("Settled"),
    PST("Failed due to Post Auth rule"),
    FPR("Failed due to fraud prevention rule"),
    PRE("Failed due to pre-auth rule");

    private final String status;

    TransactionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}