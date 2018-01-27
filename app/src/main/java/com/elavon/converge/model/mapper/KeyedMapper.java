package com.elavon.converge.model.mapper;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CardUtil;
import com.elavon.converge.util.CurrencyUtil;

import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.CustomerPresenceStatus;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionStatus;
import co.poynt.os.util.StringUtil;

public class KeyedMapper extends InterfaceMapper {
    @Override
    ElavonTransactionRequest createAuth(final Transaction t) {
        throw new ConvergeMapperException("Please implement");
    }

    @Override
    ElavonTransactionRequest createRefund(final Transaction t) {
        throw new ConvergeMapperException("Please implement");
    }

    @Override
    ElavonTransactionRequest createSale(final Transaction t) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setPoyntUserId(t.getContext().getEmployeeUserId().toString());
        request.setTransactionType(isForce(t) ? ElavonTransactionType.FORCE : ElavonTransactionType.SALE);
        request.setApprovalCode(t.getApprovalCode());
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
            }
        }

        if (CustomerPresenceStatus.PRESENT == t.getFundingSource().getEntryDetails().getCustomerPresenceStatus()) {
            request.setCardPresent(true);
        } else {
            request.setCardPresent(false);
        }
        return request;
    }

    private boolean isForce(final Transaction t) {
        return t.getStatus() == TransactionStatus.CAPTURED && StringUtil.notEmpty(t.getApprovalCode());
    }

    @Override
    ElavonTransactionRequest createVerify(final Transaction t) {
        throw new ConvergeMapperException("Please implement");
    }

    @Override
    ElavonTransactionRequest createReverse(final String t) {
        throw new ConvergeMapperException("Please implement");
    }

    @Override
    ElavonTransactionRequest createBalanceInquiry(BalanceInquiry balanceInquiry) {
        throw new ConvergeMapperException("Not supported");
    }
}
