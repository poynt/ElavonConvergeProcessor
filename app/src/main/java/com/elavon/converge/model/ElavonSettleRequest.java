package com.elavon.converge.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * <txn>
 *   <ssl_merchant_id>merchant_id</ssl_merchant_id>
 *   <ssl_user_id>user_id</ssl_user_id>
 *   <ssl_pin>pin</ssl_pin>
 *   <ssl_test_mode>false</ssl_test_mode>
 *   <ssl_transaction_type>settle</ssl_transaction_type>
 *   <ssl_transaction_id>AA48439‐65E7D601‐5E31‐4809‐882A‐2028CBC3A979</ssl_transaction_id>
 *   </txn>
 */
@Root(name = "txn")
public class ElavonSettleRequest extends ElavonRequest {

    @Root(name = "txnGroup")
    public static class TransactionGroup {
        @ElementList(entry = "ssl_txn_id", inline = true, required = false)
        private List<String> transactionIds;

        public List<String> getTransactionIds() {
            return transactionIds;
        }

        public void setTransactionIds(List<String> transactionIds) {
            this.transactionIds = transactionIds;
        }
    }

    @Element(name = "ssl_transaction_id", required = false)
    private String transactionId;

    @Element(name = "txnGroup", required = false)
    private TransactionGroup transactionGroup;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionGroup getTransactionGroup() {
        return transactionGroup;
    }

    public void setTransactionGroup(TransactionGroup transactionGroup) {
        this.transactionGroup = transactionGroup;
    }
}
