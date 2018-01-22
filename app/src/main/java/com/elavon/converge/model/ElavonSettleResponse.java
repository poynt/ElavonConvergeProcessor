package com.elavon.converge.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.math.BigDecimal;

/**
 * <txn>
 * <ssl_result>0</ssl_result>
 * <ssl_result_message>Scheduled for Settlement</ssl_result_message>
 * <ssl_txn_id/>
 * <ssl_txn_main_count>2</ssl_txn_main_count>
 * <ssl_txn_main_amount>15.00</ssl_txn_main_amount>
 * <ssl_txn_ecg_count>0</ssl_txn_ecg_count>
 * <ssl_txn_ecg_amount>0.00</ssl_txn_ecg_amount>
 * </txn>
 */
@Root(name = "txn")
public class ElavonSettleResponse extends ElavonResponse {

    @Element(name = "ssl_txn_id", required = false)
    private String txnId;

    @Element(name = "ssl_txn_main_count", required = false)
    private Integer txnMainCount;

    @Element(name = "ssl_txn_main_amount", required = false)
    private BigDecimal txnMainAmount;

    @Element(name = "ssl_txn_ecg_count", required = false)
    private Integer txnEcgCount;

    @Element(name = "ssl_txn_ecg_amount", required = false)
    private BigDecimal txnEcgAmount;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public Integer getTxnMainCount() {
        return txnMainCount;
    }

    public void setTxnMainCount(Integer txnMainCount) {
        this.txnMainCount = txnMainCount;
    }

    public BigDecimal getTxnMainAmount() {
        return txnMainAmount;
    }

    public void setTxnMainAmount(BigDecimal txnMainAmount) {
        this.txnMainAmount = txnMainAmount;
    }

    public Integer getTxnEcgCount() {
        return txnEcgCount;
    }

    public void setTxnEcgCount(Integer txnEcgCount) {
        this.txnEcgCount = txnEcgCount;
    }

    public BigDecimal getTxnEcgAmount() {
        return txnEcgAmount;
    }

    public void setTxnEcgAmount(BigDecimal txnEcgAmount) {
        this.txnEcgAmount = txnEcgAmount;
    }
}
