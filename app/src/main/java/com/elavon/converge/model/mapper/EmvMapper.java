package com.elavon.converge.model.mapper;

import com.elavon.converge.model.ElavonTransactionRequest;

import javax.inject.Inject;

import co.poynt.api.model.Transaction;

public class EmvMapper implements InterfaceMapper {

    @Inject
    public EmvMapper() {
    }

    @Override
    public ElavonTransactionRequest createAuth(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createCapture(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createVoid(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createOfflineAuth(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createRefund(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createSale(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }

    @Override
    public ElavonTransactionRequest createVerify(final Transaction transaction) {
        throw new RuntimeException("Please implement");
    }
}
