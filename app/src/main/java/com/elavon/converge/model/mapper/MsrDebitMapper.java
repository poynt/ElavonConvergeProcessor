package com.elavon.converge.model.mapper;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.AccountType;
import com.elavon.converge.model.type.ElavonPosMode;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CardUtil;
import com.elavon.converge.util.CurrencyUtil;

import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.Transaction;

import static com.elavon.converge.model.type.ElavonTransactionType.DELETE;
import static com.elavon.converge.model.type.ElavonTransactionType.VOID;

public class MsrDebitMapper extends InterfaceMapper {

    @Override
    ElavonTransactionRequest createAuth(final Transaction t) {
        throw new ConvergeMapperException("Not supported");
    }

    @Override
    ElavonTransactionRequest createRefund(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.DEBIT_RETURN);
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));

        // TODO get account type from transaction
        request.setAccountType(AccountType.CHECKING);
        request.setPinKsn(t.getFundingSource().getVerificationData().getKeySerialNumber());
        request.setKeyPointer("T");
        request.setPinBlock(t.getFundingSource().getVerificationData().getPin());
        request.setReferenceNumber(t.getProcessorResponse().getRetrievalRefNum());
        return request;
    }

    @Override
    ElavonTransactionRequest createSale(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setPoyntUserId(t.getContext().getEmployeeUserId().toString());
        request.setTransactionType(ElavonTransactionType.DEBIT_SALE);
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setPosMode(ElavonPosMode.ICC_DUAL);
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getOrderAmount(), t.getAmounts().getCurrency()));
        if (t.getAmounts().getCashbackAmount() != null) {
            request.setCashbackAmount(CurrencyUtil.getAmount(t.getAmounts().getCashbackAmount(), t.getAmounts().getCurrency()));
        }
        if (t.getAmounts().getTipAmount() != null) {
            request.setTipAmount(CurrencyUtil.getAmount(t.getAmounts().getTipAmount(), t.getAmounts().getCurrency()));
        }

        // TODO get account type from transaction
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

    @Override
    ElavonTransactionRequest createVerify(final Transaction t) {
        throw new ConvergeMapperException("Not supported");
    }

    @Override
    ElavonTransactionRequest createReverse(final String transactionId) {
        throw new ConvergeMapperException("Not supported");
    }

    @Override
    public ElavonTransactionRequest createVoid(Transaction transaction, String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        if (transaction.isAuthOnly() == Boolean.TRUE) {
            request.setTransactionType(DELETE);
        } else {
            request.setTransactionType(VOID);
        }
        // elavon transactionId
        request.setTxnId(transactionId);
        return request;
    }

    @Override
    ElavonTransactionRequest createBalanceInquiry(BalanceInquiry b) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.DEBIT_INQUIRY);
        request.setPosMode(ElavonPosMode.ICC_DUAL);
        request.setEncryptedTrackData(b.getFundingSource().getCard().getTrack2data());
        request.setKsn(b.getFundingSource().getCard().getKeySerialNumber());
        request.setExpDate(CardUtil.getCardExpiry(b.getFundingSource().getCard()));
        request.setPinKsn(b.getFundingSource().getVerificationData().getKeySerialNumber());
        request.setKeyPointer("T");
        request.setPinBlock(b.getFundingSource().getVerificationData().getPin());
        // account type - 0 checking 1 savings
        request.setAccountType(AccountType.CHECKING);

        return request;
    }
}
