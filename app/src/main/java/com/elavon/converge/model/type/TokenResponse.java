package com.elavon.converge.model.type;

public enum TokenResponse {
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    ACTION_NOT_PERMITTED("Action Not Permitted"),
    INVALID_TOKEN("Invalid Token"),
    NOT_PERMITTED("Not Permitted"),
    VERIFICATION_FAILED("Acct Verification Failed");
    
    private final String value;
    
    TokenResponse(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public String toString() {
        return value;
    }
}
