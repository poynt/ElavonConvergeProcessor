package com.elavon.converge.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * <txnlist>
 *   <txn>
 *     <ssl_result_message>APPROVAL</ssl_result_message>
 *     <ssl_txn_id>311017A15-62B81D35-08C7-4CB0-B903-37BB8196F314</ssl_txn_id>
 *     <ssl_txn_time>10/31/2017 07:50:06 PM</ssl_txn_time>
 *     <ssl_approval_code>CMC152</ssl_approval_code>
 *     <ssl_amount>16.50</ssl_amount>
 *     <ssl_conversion_rate>0.0</ssl_conversion_rate>
 *     <ssl_card_short_description>MC</ssl_card_short_description>
 *     <ssl_card_type>CREDITCARD</ssl_card_type>
 *     <errorCode>0</errorCode>
 *   </txn>
 *   <txn>
 *     <ssl_result_message>APPROVAL</ssl_result_message>
 *     <ssl_txn_id>311017A15-F7AAE3D5-8B98-4008-8F38-A5142DC4FC66</ssl_txn_id>
 *     <ssl_txn_time>10/31/2017 07:50:05 PM</ssl_txn_time>
 *     <ssl_approval_code>CMC151</ssl_approval_code>
 *     <ssl_amount>16.50</ssl_amount>
 *     <ssl_conversion_rate>0.0</ssl_conversion_rate>
 *     <ssl_card_short_description>MC</ssl_card_short_description>
 *     <ssl_card_type>CREDITCARD</ssl_card_type>
 *     <errorCode>0</errorCode>
 *   </txn>
 *   <ssl_txn_count>21</ssl_txn_count>
 * </txnlist>
 */
@Root(name = "txnlist")
public class ElavonTransactionSearchResponse extends ElavonResponse {

    @ElementList(inline = true, required = false)
    private List<ElavonTransactionResponse> list;

    @Element(name = "ssl_txn_count")
    private Integer count;

    public List<ElavonTransactionResponse> getList() {
        return list;
    }

    public void setList(List<ElavonTransactionResponse> list) {
        this.list = list;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
