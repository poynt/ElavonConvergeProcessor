package com.elavon.converge.model;

public class ProcessorCredentials {

    private String mSSLUserId;
    private String mSSLMerchantId;
    private String mSSLPin;

    public String getmSSLUserId() {
        return mSSLUserId;
    }

    public void setSSLUserId(String mSSLUserId) {
        this.mSSLUserId = mSSLUserId;
    }

    public String getSSLMerchantId() {
        return mSSLMerchantId;
    }

    public void setSSLMerchantId(String mSSLMerchantId) {
        this.mSSLMerchantId = mSSLMerchantId;
    }

    public String getSSLPin() {
        return mSSLPin;
    }

    public void setSSLPin(String mSSLPin) {
        this.mSSLPin = mSSLPin;
    }
}
