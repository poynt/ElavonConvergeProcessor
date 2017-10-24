package com.elavon.converge.model;

/**
 * Created by palavilli on 10/1/17.
 */

import org.simpleframework.xml.Element;

/**
 * <txn>
 * <ssl_merchant_id>009005</ssl_merchant_id>
 * <ssl_user_id>devportal</ssl_user_id>
 * <ssl_pin>BDDZY5KOUDCNPV4L3821K7PETO4Z7TPYOJB06TYBI1CW771IDHXBVBP51HZ6ZANJ</ssl_pin>
 * <ssl_test_mode>false</ssl_test_mode>
 * <ssl_transaction_type>ccsale</ssl_transaction_type>
 * <ssl_card_number>5472063333333330</ssl_card_number>
 * <ssl_exp_date>1225</ssl_exp_date>
 * <ssl_amount>10.00</ssl_amount>
 * <ssl_first_name>Test</ssl_first_name>
 * </txn>
 */
public class BaseModel {
    @Element(name="ssl_merchant_id")
    private String merchantId;
    @Element (name ="ssl_user_id")
    private String userId;
    @Element (name ="ssl_pin")
    private String pin;
    @Element (name ="ssl_test_mode", required=false)
    private String testMode;

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
}
