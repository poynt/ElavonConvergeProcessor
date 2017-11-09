package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;

import co.poynt.api.model.Transaction;

public interface InterfaceMapper {
    ElavonTransactionRequest createAuth(Transaction transaction);
    ElavonTransactionRequest createCapture(Transaction transaction);
    ElavonTransactionRequest createVoid(Transaction transaction);
    ElavonTransactionRequest createOfflineAuth(Transaction transaction);
    ElavonTransactionRequest createRefund(Transaction transaction);
    ElavonTransactionRequest createSale(Transaction transaction);
    ElavonTransactionRequest createVerify(Transaction transaction);
}
