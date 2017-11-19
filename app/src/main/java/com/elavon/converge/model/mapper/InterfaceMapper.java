package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.util.CurrencyUtil;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.Transaction;

import static com.elavon.converge.model.type.ElavonTransactionType.VOID;

public abstract class InterfaceMapper {
    abstract ElavonTransactionRequest createAuth(Transaction transaction);

    abstract ElavonTransactionRequest createRefund(Transaction transaction);

    abstract ElavonTransactionRequest createSale(Transaction transaction);

    abstract ElavonTransactionRequest createVerify(Transaction transaction);

    abstract ElavonTransactionRequest createReverse(String transactionId);

    public ElavonTransactionRequest createCapture(String transactionId,
                                                  AdjustTransactionRequest adjustTransactionRequest) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(ElavonTransactionType.COMPLETE);

        // elavon transactionId
        request.setTxnId(transactionId);
        // update tip if customer did not opted No Tip
        if (adjustTransactionRequest.getAmounts() != null) {
            request.setAmount(CurrencyUtil.getAmount(
                    adjustTransactionRequest.getAmounts().getTransactionAmount(),
                    adjustTransactionRequest.getAmounts().getCurrency()));
        }

        //NOTE: neither tip nor signature supported in complete request

        return request;
    }

    public ElavonTransactionRequest createVoid(String transactionId) {
        final ElavonTransactionRequest request = new ElavonTransactionRequest();
        request.setTransactionType(VOID);
        // elavon transactionId
        request.setTxnId(transactionId);
        return request;
    }

}
