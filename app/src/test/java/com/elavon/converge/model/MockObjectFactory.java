package com.elavon.converge.model;

public class MockObjectFactory {

    public static ElavonTransaction getElavonTransaction() {
        ElavonTransaction txn = new ElavonTransaction();
        txn.setUserId("user");
        txn.setPin("pin");
        txn.setMerchantId("merchant");
        txn.setTransactionType(ElavonTransactionType.SALE);
        return txn;
    }
}
