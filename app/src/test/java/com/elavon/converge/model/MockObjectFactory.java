package com.elavon.converge.model;

public class MockObjectFactory {

    public static ElavonTransactionRequest getElavonTransactionRequest() {
        ElavonTransactionRequest txn = new ElavonTransactionRequest();
        txn.setUserId("user");
        txn.setPin("pin");
        txn.setMerchantId("merchant");
        txn.setTransactionType(ElavonTransactionType.SALE);
        return txn;
    }
}
