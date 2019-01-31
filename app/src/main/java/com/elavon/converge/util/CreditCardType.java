package com.elavon.converge.util;

import java.util.regex.Pattern;

/**
 * Created by palavilli on 10/22/15.
 */
public enum CreditCardType {
    UNKNOWN,
    VISA("^4[0-9]{5}$"),
    MASTERCARD("^5[1-5][0-9]{4}$"),
    AMERICAN_EXPRESS("^3[47][0-9]{4}$"),
    DINERS_CLUB("^3(?:0[0-5][0-9]{3}|[68][0-9]{4})$"),
    DISCOVER("^6(?:011|5[0-9]{2})[0-9]{2}$"),
    JCB("^(?:2131|1800|35\\d{2})\\d{2}$"),
    MAESTRO("^67[0-9]{4}$");

    private Pattern pattern;

    CreditCardType() {
        this.pattern = null;
    }

    CreditCardType(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public static CreditCardType detect(String cardNumber) {

        for (CreditCardType cardType : CreditCardType.values()) {
            if (null == cardType.pattern) continue;
            if (cardType.pattern.matcher(cardNumber).matches()) return cardType;
        }

        return UNKNOWN;
    }

}
