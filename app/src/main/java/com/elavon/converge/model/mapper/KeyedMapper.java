package com.elavon.converge.model.mapper;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonEntryMode;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CardUtil;
import com.elavon.converge.util.CurrencyUtil;

import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.CVSkipReason;
import co.poynt.api.model.CustomerPresenceStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionStatus;
import co.poynt.os.util.StringUtil;

import static com.elavon.converge.model.type.ElavonTransactionType.DELETE;
import static com.elavon.converge.model.type.ElavonTransactionType.VOID;

public class KeyedMapper extends InterfaceMapper {
    @Override
    ElavonTransactionRequest createAuth(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        if (transaction.getFundingSource().getEntryDetails().isIccFallback()
                == Boolean.TRUE) {
            request.setTransactionType(ElavonTransactionType.EMV_SWIPE_AUTH_ONLY);
        } else {
            request.setTransactionType(ElavonTransactionType.AUTH_ONLY);
        }
        return request;
    }

    @Override
    ElavonTransactionRequest createRefund(final Transaction t) {
        ElavonTransactionRequest request = null;
        // if action in transaction is refund but no parent id then it's
        // a Non-Ref Credit otherwise it's a regular refund
        if (t.getParentId() != null) {
            request = new ElavonTransactionRequest();
            request.setTransactionType(ElavonTransactionType.RETURN);
            request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
            // add retrieval ref number if we have it
            if (t.getProcessorResponse() != null) {
                request.setTxnId(t.getProcessorResponse().getRetrievalRefNum());
            }
        } else {
            request = createRequest(t);
            request.setTransactionType(ElavonTransactionType.CREDIT);
        }
        return request;
    }

    @Override
    ElavonTransactionRequest createSale(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        request.setTransactionType(isForce(transaction) ? ElavonTransactionType.FORCE : ElavonTransactionType.SALE);
        request.setApprovalCode(transaction.getApprovalCode());
        return request;
    }

    private boolean isForce(final Transaction t) {
        return t.getStatus() == TransactionStatus.CAPTURED && StringUtil.notEmpty(t.getApprovalCode());
    }

    @Override
    ElavonTransactionRequest createVerify(final Transaction transaction) {
        final ElavonTransactionRequest request = createRequest(transaction);
        request.setTransactionType(ElavonTransactionType.VERIFY);
        return request;
    }

    @Override
    ElavonTransactionRequest createReverse(final String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(VOID);
        // elavon transactionId
        request.setTxnId(transactionId);
        return request;
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
    ElavonTransactionRequest createBalanceInquiry(BalanceInquiry balanceInquiry) {
        throw new ConvergeMapperException("Not supported");
    }

    private ElavonTransactionRequest createRequest(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();

        if (CustomerPresenceStatus.PRESENT == t.getFundingSource().getEntryDetails().getCustomerPresenceStatus()) {
            request.setCardPresent(true);
            request.setEntryMode(ElavonEntryMode.KEY_ENTERED_CARD_PRESENT);
        } else {
            request.setCardPresent(false);
            request.setEntryMode(ElavonEntryMode.KEY_ENTERED_CARD_NOT_PRESENT);
        }

        request.setPoyntUserId(t.getContext().getEmployeeUserId().toString());
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
        // add card token if we have it
        // TODO - is this the right field ?
        if (StringUtil.notEmpty(t.getFundingSource().getCard().getNumberHashed())) {
            request.setToken(t.getFundingSource().getCard().getNumberHashed());
        }
        request.setExpDate(CardUtil.getCardExpiry(t.getFundingSource().getCard()));
        request.setCardLast4(t.getFundingSource().getCard().getNumberLast4());
        // add card number if we have it
        if (StringUtil.notEmpty(t.getFundingSource().getCard().getNumber())) {
            request.setCardNumber(t.getFundingSource().getCard().getNumber());
        }
        // add KSN if we have it
        if (StringUtil.notEmpty(t.getFundingSource().getCard().getKeySerialNumber())) {
            request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        }

        // verification info
        if (t.getFundingSource().getVerificationData() != null) {
            if (t.getFundingSource().getVerificationData().getCardHolderBillingAddress() != null) {
                if (StringUtil.notEmpty(t.getFundingSource().getVerificationData().getCardHolderBillingAddress().getPostalCode())) {
                    request.setAvsZip(t.getFundingSource().getVerificationData().getCardHolderBillingAddress().getPostalCode());
                }
                if (StringUtil.notEmpty(t.getFundingSource().getVerificationData().getCardHolderBillingAddress().getLine1())) {
                    request.setAvsAddress(t.getFundingSource().getVerificationData().getCardHolderBillingAddress().getLine1());
                }
            }
            if (StringUtil.notEmpty(t.getFundingSource().getVerificationData().getCvData())) {
                request.setCvv2(t.getFundingSource().getVerificationData().getCvData());
                // 1 for present
                request.setCvv2Indicator("1");
            } else if (t.getFundingSource().getVerificationData().getCvSkipReason() != null) {
                if (t.getFundingSource().getVerificationData().getCvSkipReason() == CVSkipReason.NOT_PRESENT) {
                    request.setCvv2Indicator("9");
                } else if (t.getFundingSource().getVerificationData().getCvSkipReason() == CVSkipReason.ILLEGIBLE) {
                    request.setCvv2Indicator("2");
                } else if (t.getFundingSource().getVerificationData().getCvSkipReason() == CVSkipReason.BYPASSED) {
                    request.setCvv2Indicator("0");
                } else if (t.getFundingSource().getVerificationData().getCvSkipReason() == CVSkipReason.NOT_AVAILABLE) {
                    request.setCvv2Indicator("9");
                }
            }
        }
        return request;
    }
}
