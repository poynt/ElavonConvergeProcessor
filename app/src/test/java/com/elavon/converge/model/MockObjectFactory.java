package com.elavon.converge.model;

import com.elavon.converge.model.type.ElavonTransactionType;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class MockObjectFactory {
    public static ElavonTransactionRequest getElavonTransactionRequest() {
        final ElavonTransactionRequest txn = new ElavonTransactionRequest();
        txn.setMerchantId("someId");
        txn.setUserId("someId");
        txn.setPin("somePin");
        txn.setTestMode("false");
        txn.setTransactionType(ElavonTransactionType.SALE);
        txn.setCardNumber("5472063333333330");
        txn.setCvv2("123");
        txn.setExpDate("1225");
//        txn.setCardNumber("4124939999999990");
//        txn.setExpDate("1219");
        txn.setAmount(new BigDecimal(16.50).setScale(2, BigDecimal.ROUND_HALF_UP));
        txn.setFirstName("Poynt");
        txn.setLastName("Boynt");
        txn.setDescription("tables & chairs");
        txn.setMerchantTxnId(UUID.randomUUID().toString());
        txn.setInvoiceNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 25));
        return txn;
    }

    public static ElavonTransactionRequest getElavonTransactionUpdateRequest(final String transactionId) {
        final ElavonTransactionRequest txn = new ElavonTransactionRequest();
        txn.setTransactionType(ElavonTransactionType.UPDATE_TIP);
        txn.setTxnId(transactionId);
        txn.setTipAmount(BigDecimal.ONE);
        return txn;
    }

    public static ElavonTransactionSearchRequest getElavonTransactionSearchRequest() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, -1);

        final ElavonTransactionSearchRequest search = new ElavonTransactionSearchRequest();
        search.setMerchantId("someId");
        search.setUserId("someId");
        search.setPin("somePin");
        search.setTestMode("false");
        search.setTransactionType(ElavonTransactionType.TRANSACTION_QUERY);
        search.setCardSuffix("3330");
        search.setSearchStartDate(cal.getTime());
        return search;
    }
}
