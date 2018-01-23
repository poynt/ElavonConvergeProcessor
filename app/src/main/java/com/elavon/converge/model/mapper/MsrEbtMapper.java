package com.elavon.converge.model.mapper;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CurrencyUtil;

import java.util.HashMap;
import java.util.Map;

import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.EBTType;
import co.poynt.api.model.Transaction;

public class MsrEbtMapper extends InterfaceMapper {

    private static final Map<EBTType, ElavonTransactionType> EBT_SALE_TYPES_MAP = new HashMap<EBTType, ElavonTransactionType>() {{
        put(EBTType.FOOD_STAMP,ElavonTransactionType.EBT_SALE);
        put(EBTType.CASH_BENEFIT,ElavonTransactionType.EBT_CASH_SALE);
    }};
    private static final Map<EBTType, ElavonTransactionType> EBT_REFUND_TYPES_MAP = new HashMap<EBTType, ElavonTransactionType>() {{
        put(EBTType.FOOD_STAMP, ElavonTransactionType.EBT_RETURN);
    }};

    @Override
    ElavonTransactionRequest createAuth(final Transaction t) {
        throw new ConvergeMapperException("Auth not allowed in EBT transaction");
    }

    @Override
    ElavonTransactionRequest createRefund(final Transaction t) {
        final EBTType ebtType = t.getFundingSource().getEbtDetails().getType();
        if (!EBT_REFUND_TYPES_MAP.containsKey(ebtType)) {
            throw new ConvergeMapperException("Not supported EBT type for MSR: " + ebtType);
        }

        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(EBT_REFUND_TYPES_MAP.get(ebtType));
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
        request.setPinKsn(t.getFundingSource().getVerificationData().getKeySerialNumber());
        request.setKeyPointer("T");
        request.setPinBlock(t.getFundingSource().getVerificationData().getPin());
        return request;
    }

    @Override
    ElavonTransactionRequest createSale(final Transaction t) {
        final EBTType ebtType = t.getFundingSource().getEbtDetails().getType();
        if (!EBT_SALE_TYPES_MAP.containsKey(ebtType)) {
            throw new ConvergeMapperException("Not supported EBT type for MSR: " + ebtType);
        }

        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setPoyntUserId(t.getContext().getEmployeeUserId().toString());
        request.setTransactionType(EBT_SALE_TYPES_MAP.get(ebtType));
        request.setEncryptedTrackData(t.getFundingSource().getCard().getTrack2data());
        request.setKsn(t.getFundingSource().getCard().getKeySerialNumber());
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getOrderAmount(), t.getAmounts().getCurrency()));
        if (t.getAmounts().getCashbackAmount() != null) {
            request.setCashbackAmount(CurrencyUtil.getAmount(t.getAmounts().getCashbackAmount(), t.getAmounts().getCurrency()));
        }
        request.setPinKsn(t.getFundingSource().getVerificationData().getKeySerialNumber());
        request.setKeyPointer("T");
        request.setPinBlock(t.getFundingSource().getVerificationData().getPin());
        return request;
    }

    @Override
    ElavonTransactionRequest createVerify(final Transaction t) {
        throw new ConvergeMapperException("Verify not allowed in EBT transaction");
    }

    @Override
    ElavonTransactionRequest createReverse(final String t) {
        throw new ConvergeMapperException("Reverse not allowed in EBT transaction");
    }

    @Override
    ElavonTransactionRequest createBalanceInquiry(BalanceInquiry balanceInquiry) {
        throw new ConvergeMapperException("Not supported");
    }
}
