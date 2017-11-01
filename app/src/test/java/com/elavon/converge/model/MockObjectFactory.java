package com.elavon.converge.model;

import com.elavon.converge.model.type.ElavonTransactionType;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

public class MockObjectFactory {
    public static ElavonTransactionRequest getElavonTransactionRequest() {
        final ElavonTransactionRequest txn = new ElavonTransactionRequest();
        txn.setMerchantId("009005");
        txn.setUserId("devportal");
        txn.setPin("BDDZY5KOUDCNPV4L3821K7PETO4Z7TPYOJB06TYBI1CW771IDHXBVBP51HZ6ZANJ");
        txn.setTestMode("false");
        txn.setTransactionType(ElavonTransactionType.SALE);
        txn.setCardNumber("5472063333333330");
        txn.setExpDate("1225");
//        txn.setCardNumber("4124939999999990");
//        txn.setExpDate("1219");
        txn.setAmount(new BigDecimal(16.50).setScale(2, BigDecimal.ROUND_HALF_UP));
        txn.setFirstName("Rando");
        txn.setDescription("tables & chairs");
        return txn;
    }

    public static ElavonTransactionSearchRequest getElavonTransactionSearchRequest() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, -1);

        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setMerchantId("009005");
        search.setUserId("devportal");
        search.setPin("BDDZY5KOUDCNPV4L3821K7PETO4Z7TPYOJB06TYBI1CW771IDHXBVBP51HZ6ZANJ");
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setCardSuffix("3330");
        search.setSearchStartDate(cal.getTime());
        return search;
    }
}
