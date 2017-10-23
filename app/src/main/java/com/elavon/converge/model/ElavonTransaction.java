package com.elavon.converge.model;

/**
 * Created by palavilli on 10/1/17.
 */

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.math.BigDecimal;

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

@Root(name="txn")
public class ElavonTransaction extends BaseModel {
    //@Element (name="ssl_transaction_type")
    @Element(name="ssl_transaction_type")
    private ElavonTransactionType transactionType;
    @Element (name ="ssl_card_number", required=false)
    private String cardNumber;
    @Element (name ="ssl_exp_date", required=false)
    /**
     * Do not send an expiration date with a token that is stored in the Card Manager.
     */
    private String expDate;
    @Element (name = "ssl_enc_track_data", required=false)
    private String encryptedTrackData;
    @Element (name="ssl_ksn", required = false)
    private String ksn;
    /**
     * When specifying amounts, be sure to submit the correct number of decimal places for the transaction currency.
     * For Japanese Yen, there are no currency exponents after the decimal place. Any numbers included
     * after the decimalplace will be dropped.
     * For those currencies with 3 possible digits, for example: Bahraini Dinar, we will automatically
     * round the transaction to two decimals. (forexample: 1.235 will round to 1.23).
     */
    @Element (name ="ssl_amount", required=false)
    private String amount;
    @Element (name ="ssl_cvv2cvc2_indicator", required=false)
    private String cvv2Indicator;
    @Element (name ="ssl_cvv2cvc2", required=false)
    private String cvv2;
    @Element (name ="ssl_first_name", required=false)
    private String firstName;


    public ElavonTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(ElavonTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getEncryptedTrackData() {
        return encryptedTrackData;
    }

    public void setEncryptedTrackData(String encryptedTrackData) {
        this.encryptedTrackData = encryptedTrackData;
    }

    public String getKsn() {
        return ksn;
    }

    public void setKsn(String ksn) {
        this.ksn = ksn;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCvv2Indicator() {
        return cvv2Indicator;
    }

    public void setCvv2Indicator(String cvv2Indicator) {
        this.cvv2Indicator = cvv2Indicator;
    }

    public String getCvv2() {
        return cvv2;
    }

    public void setCvv2(String cvv2) {
        this.cvv2 = cvv2;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


}
