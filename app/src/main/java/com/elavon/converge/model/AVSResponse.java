package com.elavon.converge.model;

/**
 * Created by dennis on 10/24/17.
 */

public enum AVSResponse {
    STREET_MATCH("A"), // Address matches - ZIP Code does not match
    STREET_MATCH_MALFORMED_ZIP("B"), // Street address match, Postal code in wrong format (international issuer)
    MALFORMED_STREET_ZIP("C"), // Street address and postal code in wrong formats
    MATCH_INTERNATIONAL("D"), // Street address and postal code match (international issuer)
    AVS_ERROR("E"), // AVS Error
    MATCH_UK("F"), // Address does compare (i.e. match) and five-digit ZIP code does compare (UK only)
    NOT_SUPPORTED_INTL("G"), // Service not supported by non-US issuer
    NOT_VERIFIED("I"), // Address information not verified by international issuer
    MATCH_INTERNATIONAL_INTL("M"), // Street Address and Postal code match (international issuer)
    NO_MATCH("N"), // No Match on Address (Street) or ZIP
    NO_RESPONSE("O"), // No Response sent
    ZIP_MATCH("P"), // Postal codes match, Street address not verified due to incompatible formats
    UNAVAILABLE("R"), // Retry, System unavailable or Timed out
    NOT_SUPPORTED("S"), // Service not supported by issuer
    ADDRESS_UNAVAILABLE("U"), // Address information is unavailable
    ZIP9_MATCH("W"), // 9-digit ZIP matches, Address (Street) does not match
    MATCH("X"), // Exact AVS Match
    STREET_ZIP5_MATCH("Y"), // Address (Street) and 5-digit ZIP match
    ZIP5_MATCH("Z"); // 5-digit ZIP matches, Address (Street) does not match
    
    private final String value;
    
    AVSResponse(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public String toString() {
        return value;
    }
}
