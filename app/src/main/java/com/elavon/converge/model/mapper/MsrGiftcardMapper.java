package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CurrencyUtil;

import co.poynt.api.model.Transaction;

public class MsrGiftcardMapper extends InterfaceMapper {
    @Override
    ElavonTransactionRequest createAuth(final Transaction t) {
        throw new RuntimeException("Please implement");
    }

    @Override
    ElavonTransactionRequest createRefund(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.GIFT_CARD_REFUND);
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
        if (t.getAmounts().getTipAmount() != null) {
            request.setTipAmount((CurrencyUtil.getAmount(t.getAmounts().getTipAmount(), t.getAmounts().getCurrency())));
        }
        return request;
    }

    @Override
    ElavonTransactionRequest createSale(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setPoyntUserId(t.getContext().getEmployeeUserId().toString());
        request.setTransactionType(ElavonTransactionType.GIFT_CARD_SALE);
        // TODO for activation
        // request.setGiftcardTenderType("1");
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
        if (t.getAmounts().getTipAmount() != null) {
            request.setTipAmount((CurrencyUtil.getAmount(t.getAmounts().getTipAmount(), t.getAmounts().getCurrency())));
        }
        return request;
    }

    @Override
    ElavonTransactionRequest createVerify(final Transaction t) {
        throw new RuntimeException("Please implement");
    }

    @Override
    ElavonTransactionRequest createReverse(final String transactionId) {
        throw new RuntimeException("Please implement");
    }
}
