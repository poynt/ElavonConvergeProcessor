package com.elavon.converge.util;

import co.poynt.api.model.Card;

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
}
