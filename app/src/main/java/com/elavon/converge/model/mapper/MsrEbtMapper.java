package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;

import co.poynt.api.model.Transaction;

public class MsrEbtMapper extends InterfaceMapper {
    @Override
    ElavonTransactionRequest createAuth(Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    ElavonTransactionRequest createRefund(Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    ElavonTransactionRequest createSale(Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    ElavonTransactionRequest createVerify(Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    ElavonTransactionRequest createReverse(String transactionId) {
        throw new RuntimeException("Please implement");
    }
}
