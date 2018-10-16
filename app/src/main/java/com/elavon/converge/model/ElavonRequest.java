package com.elavon.converge.model;

import com.elavon.converge.model.type.ElavonTransactionType;

import org.simpleframework.xml.Element;

public abstract class ElavonRequest {

    @Element(name = "ssl_vendor_id")
    private String vendorId;

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


    @Element(name = "Poynt_Device_User")
    private String deviceUser;

    @Element(name = "ssl_vendor_app_version")
    private String appVersion;

    @Element(name = "ssl_vendor_app_name")
    private String appName;


    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setDeviceUser(String deviceUser) {
        this.deviceUser = deviceUser;
    }

    public String getDeviceUser() {
        return deviceUser;
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

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
