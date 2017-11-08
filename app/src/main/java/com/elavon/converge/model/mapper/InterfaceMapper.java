package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;

import java.math.BigDecimal;
import java.util.Currency;

import co.poynt.api.model.Card;
import co.poynt.api.model.Transaction;

public abstract class InterfaceMapper {
    abstract ElavonTransactionRequest createAuth(Transaction transaction);
    abstract ElavonTransactionRequest createCapture(Transaction transaction);
    abstract ElavonTransactionRequest createVoid(Transaction transaction);
    abstract ElavonTransactionRequest createOfflineAuth(Transaction transaction);
    abstract ElavonTransactionRequest createRefund(Transaction transaction);
    abstract ElavonTransactionRequest createSale(Transaction transaction);
    abstract ElavonTransactionRequest createVerify(Transaction transaction);

    protected String getCardExpiry(final Card card) {
        if (card == null || card.getExpirationMonth() == null || card.getExpirationYear() == null) {
            return null;
        }
        return card.getExpirationMonth().toString() + (card.getExpirationYear() % 100);
    }

    protected BigDecimal getAmount(final Long amount, final String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        final Currency cur = Currency.getInstance(currency);
        final BigDecimal pow = BigDecimal.valueOf(10L).pow(cur.getDefaultFractionDigits());
        return BigDecimal.valueOf(amount).divide(pow);
    }
}
