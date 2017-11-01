package com.elavon.converge.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.Date;

/**
 * <txn>
 *   <ssl_merchant_id>merchant_id</ssl_merchant_id>
 *   <ssl_user_id>user_id</ssl_user_id>
 *   <ssl_pin>pin</ssl_pin>
 *   <ssl_test_mode>false</ssl_test_mode>
 *   <ssl_transaction_type>txnquery</ssl_transaction_type>
 *   <ssl_card_suffix>3330</ssl_card_suffix>
 *   <ssl_search_start_date>10/31/2017 06:53:53 PM</ssl_search_start_date>
 * </txn>
 */
@Root(name = "txn")
public class ElavonTransactionSearchRequest extends ElavonRequest {

    @Element(name = "ssl_txn_id", required = false)
    private String transactionId;

    @Element(name = "ssl_card_number", required = false)
    private String cardNumber;

    @Element(name = "ssl_track_data", required = false)
    private String trackData;

    @Element(name = "ssl_card_suffix", required = false)
    private String cardSuffix;

    @Element(name = "ssl_search_start_date", required = false)
    private Date searchStartDate;

    @Element(name = "ssl_search_end_date", required = false)
    private Date searchEndDate;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getTrackData() {
        return trackData;
    }

    public void setTrackData(String trackData) {
        this.trackData = trackData;
    }

    public String getCardSuffix() {
        return cardSuffix;
    }

    public void setCardSuffix(String cardSuffix) {
        this.cardSuffix = cardSuffix;
    }

    public Date getSearchStartDate() {
        return searchStartDate;
    }

    public void setSearchStartDate(Date searchStartDate) {
        this.searchStartDate = searchStartDate;
    }

    public Date getSearchEndDate() {
        return searchEndDate;
    }

    public void setSearchEndDate(Date searchEndDate) {
        this.searchEndDate = searchEndDate;
    }
}
