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

import static com.elavon.converge.model.type.ElavonTransactionType.DELETE;
import static com.elavon.converge.model.type.ElavonTransactionType.VOID;

public class MsrEbtMapper extends InterfaceMapper {

    private static final Map<EBTType, ElavonTransactionType> EBT_SALE_TYPES_MAP = new HashMap<EBTType, ElavonTransactionType>() {{
        put(EBTType.FOOD_STAMP, ElavonTransactionType.EBT_SALE);
        put(EBTType.CASH_BENEFIT, ElavonTransactionType.EBT_CASH_SALE);
        put(EBTType.FOOD_STAMP_ELECTRONIC_VOUCHER, ElavonTransactionType.EBT_FORCE_SALE);
    }};
    private static final Map<EBTType, ElavonTransactionType> EBT_REFUND_TYPES_MAP = new HashMap<EBTType, ElavonTransactionType>() {{
        put(EBTType.FOOD_STAMP, ElavonTransactionType.EBT_RETURN);
        put(EBTType.FOOD_STAMP_ELECTRONIC_VOUCHER, ElavonTransactionType.EBT_FORCE_RETURN);
    }};
    private static final Map<EBTType, ElavonTransactionType> EBT_INQUIRY_TYPES_MAP = new HashMap<EBTType, ElavonTransactionType>() {{
        put(EBTType.FOOD_STAMP, ElavonTransactionType.EBT_INQUIRY);
        put(EBTType.CASH_BENEFIT, ElavonTransactionType.EBT_CASH_INQUIRY);
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
        if (t.getAmounts().getOrderAmount() != null) {
            request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getOrderAmount(),
                    t.getAmounts().getCurrency()));
        } else {
            request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(),
                    t.getAmounts().getCurrency()));
        }

        if (t.getAmounts().getCashbackAmount() != null) {
            request.setCashbackAmount(CurrencyUtil.getAmount(t.getAmounts().getCashbackAmount(),
                    t.getAmounts().getCurrency()));
        }

        if (t.getFundingSource().getVerificationData() != null) {
            request.setPinKsn(t.getFundingSource().getVerificationData().getKeySerialNumber());
            request.setPinBlock(t.getFundingSource().getVerificationData().getPin());
        }
        request.setKeyPointer("T");
        if (ebtType == EBTType.FOOD_STAMP_ELECTRONIC_VOUCHER) {
            request.setApprovalCode(t.getFundingSource().getEbtDetails().getElectronicVoucherApprovalCode());
            request.setVoucherNumber(t.getFundingSource().getEbtDetails().getElectronicVoucherSerialNumber());
        }
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
    ElavonTransactionRequest createBalanceInquiry(final BalanceInquiry b) {
        final EBTType ebtType = b.getFundingSource().getEbtDetails().getType();
        if (!EBT_INQUIRY_TYPES_MAP.containsKey(ebtType)) {
            throw new ConvergeMapperException("Not supported EBT type for MSR: " + ebtType);
        }

        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(EBT_INQUIRY_TYPES_MAP.get(ebtType));
        request.setEncryptedTrackData(b.getFundingSource().getCard().getTrack2data());
        request.setKsn(b.getFundingSource().getCard().getKeySerialNumber());

        request.setPinKsn(b.getFundingSource().getVerificationData().getKeySerialNumber());
        request.setKeyPointer("T");
        request.setPinBlock(b.getFundingSource().getVerificationData().getPin());
        return request;
    }
}
