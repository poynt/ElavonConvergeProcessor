package com.elavon.converge.model.mapper;

import com.elavon.converge.exception.ConvergeMapperException;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CardUtil;
import com.elavon.converge.util.CurrencyUtil;

import co.poynt.api.model.EBTType;
import co.poynt.api.model.Transaction;

public class KeyedEbtMapper extends InterfaceMapper {
    @Override
    ElavonTransactionRequest createAuth(final Transaction t) {
        throw new ConvergeMapperException("Auth not allowed in EBT transaction");
    }

    @Override
    ElavonTransactionRequest createRefund(final Transaction t) {
        throw new ConvergeMapperException("Please implement");
    }

    @Override
    ElavonTransactionRequest createSale(final Transaction t) {
        // converge api only allows keyed entry for food stamp voucher
        if (t.getFundingSource().getEbtDetails().getType() != EBTType.FOOD_STAMP_ELECTRONIC_VOUCHER) {
            throw new ConvergeMapperException("Only food stamp electronic voucher allowed for keyed EBT");
        }

        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.EBT_FORCE_SALE);
        // TODO double check if converge allowes encrypted card number
        request.setCardNumber(t.getFundingSource().getCard().getNumber());
        request.setExpDate(CardUtil.getCardExpiry(t.getFundingSource().getCard()));
        request.setAmount(CurrencyUtil.getAmount(t.getAmounts().getTransactionAmount(), t.getAmounts().getCurrency()));
        request.setApprovalCode(t.getFundingSource().getEbtDetails().getElectronicVoucherApprovalCode());
        request.setVoucherNumber(t.getFundingSource().getEbtDetails().getElectronicVoucherSerialNumber());
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
}
