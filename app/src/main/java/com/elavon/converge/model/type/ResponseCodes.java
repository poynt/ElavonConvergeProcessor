package com.elavon.converge.model.type;

public enum ResponseCodes {

    AA("APPROVAL"),
    AP("PARTIAL APPROVAL"),
    N7("DECLINE CVV2"),
    NC("PICK UP CARD"),
    ND("DECLINED"),
    NR("REFER TO ISSUER");

    private final String message;

    ResponseCodes(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}