package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.AccountType;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CardUtil;
import com.elavon.converge.util.CurrencyUtil;

import co.poynt.api.model.Transaction;

public class MsrDebitMapper extends InterfaceMapper {
    @Override
    ElavonTransactionRequest createAuth(final Transaction t) {
        throw new RuntimeException("Please implement");
    }

    @Override
    ElavonTransactionRequest createRefund(final Transaction t) {
        throw new RuntimeException("Please implement");
    }

    @Override
    ElavonTransactionRequest createSale(final Transaction t) {
        final ElavonTransactionRequest request = createRequest(t);
        request.setTransactionType(ElavonTransactionType.DEBIT_SALE);
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

    private ElavonTransactionRequest createRequest(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getOrderAmount(), t.getAmounts().getCurrency()));
        if (t.getAmounts().getCashbackAmount() != null) {
            request.setCashbackAmount(CurrencyUtil.getAmount(t.getAmounts().getCashbackAmount(), t.getAmounts().getCurrency()));
        }
        if (t.getAmounts().getTipAmount() != null) {
            request.setTipAmount(CurrencyUtil.getAmount(t.getAmounts().getTipAmount(), t.getAmounts().getCurrency()));
        }

        request.setAccountType(AccountType.CHECKING);
        request.setPinKsn(t.getFundingSource().getVerificationData().getKeySerialNumber());
        request.setKeyPointer("T");
        request.setPinBlock(t.getFundingSource().getVerificationData().getPin());
        request.setFirstName(t.getFundingSource().getCard().getCardHolderFirstName());
        request.setLastName(t.getFundingSource().getCard().getCardHolderLastName());
        request.setExpDate(CardUtil.getCardExpiry(t.getFundingSource().getCard()));
        request.setCardLast4(t.getFundingSource().getCard().getNumberLast4());
        return request;
    }
}
