package com.elavon.converge.model;

import com.elavon.converge.model.type.ElavonTransactionType;

import org.simpleframework.xml.Element;

public abstract class ElavonRequest {

    @Element(name = "ssl_merchant_id")
    private String merchantId;

    @Element(name = "ssl_user_id")
    private String userId;

    @Element(name = "ssl_pin")
    private String pin;

    @Element(name = "ssl_test_mode", required = false)
    private String testMode;

    @Element(name = "ssl_transaction_type")
    private ElavonTransactionType transactionType;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getTestMode() {
        return testMode;
    }

    public void setTestMode(String testMode) {
        this.testMode = testMode;
    }

    public ElavonTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(ElavonTransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
