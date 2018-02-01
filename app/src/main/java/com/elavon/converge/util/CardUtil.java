package com.elavon.converge.util;

import co.poynt.api.model.Card;
import co.poynt.api.model.CardType;

public class CardUtil {
    /**
     * Returns card expiry in a single string. Eg. 1219 for 2019-12
     */
    public static String getCardExpiry(final Card card) {
        if (card == null || card.getExpirationMonth() == null || card.getExpirationYear() == null) {
            return null;
        }
        return card.getExpirationMonth().toString() + (card.getExpirationYear() % 100);
    }

    public static CardType cardTypeByFirst6(String first6)
            throws NumberFormatException {
        if (first6 == null)
            throw new NumberFormatException("Null credit card number");

        CreditCardType creditCardType = CreditCardType.detect(first6);
        switch (creditCardType) {
            case VISA:
                return CardType.VISA;
            case MASTERCARD:
                return CardType.MASTERCARD;
            case AMERICAN_EXPRESS:
                return CardType.AMERICAN_EXPRESS;
            case DISCOVER:
                return CardType.DISCOVER;
            case DINERS_CLUB:
                return CardType.DINERS_CLUB;
            case JCB:
                return CardType.JCB;
            case MAESTRO:
                return CardType.MAESTRO;
            case UNKNOWN:
            default:
                return CardType.OTHER;
        }
    }
}
