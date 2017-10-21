package com.elavon.converge.model;

/**
 * Created by palavilli on 10/1/17.
 */

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

public class ElavonTransaction extends BaseModel {
    private String transactionType;
    private String cardNumber;
    private String expDate;
    private String firstName;
    private BigDecimal amount;
}
