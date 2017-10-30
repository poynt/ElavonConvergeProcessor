package com.elavon.converge.model;

import com.elavon.converge.model.type.ElavonTransactionType;

import java.math.BigDecimal;

public class MockObjectFactory {
    public static ElavonTransactionRequest getElavonTransactionRequest() {
        final ElavonTransactionRequest txn = new ElavonTransactionRequest();
        txn.setMerchantId("009005");
        txn.setUserId("devportal");
        txn.setPin("BDDZY5KOUDCNPV4L3821K7PETO4Z7TPYOJB06TYBI1CW771IDHXBVBP51HZ6ZANJ");
        txn.setTestMode("false");
        txn.setTransactionType(ElavonTransactionType.SALE);
//        txn.setCardNumber("5472063333333330");
//        txn.setExpDate("1225");
        txn.setCardNumber("4124939999999990");
        txn.setExpDate("1219");
        txn.setAmount(new BigDecimal(10.11).setScale(2, BigDecimal.ROUND_HALF_UP));
        txn.setFirstName("Rando");
        txn.setDescription("tables & chairs");
        return txn;
    }
}
