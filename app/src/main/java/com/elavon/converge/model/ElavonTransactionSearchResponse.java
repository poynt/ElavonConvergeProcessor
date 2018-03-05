package com.elavon.converge.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * <txnlist>
 * <ssl_txn_count>7</ssl_txn_count>
 * <txn>
 * <ssl_txn_id>050218A15-68CE063B-2B4C-40E4-9E6C-AF86257040CC</ssl_txn_id>
 * <ssl_user_id>008382</ssl_user_id>
 * <ssl_trans_status>STL</ssl_trans_status>
 * <ssl_card_type>CREDITCARD</ssl_card_type>
 * <ssl_card_short_description>VISA</ssl_card_short_description>
 * <ssl_transaction_type>SALE</ssl_transaction_type>
 * <ssl_txn_time>02/05/2018 05:38:17 PM</ssl_txn_time>
 * <ssl_first_name>TESTCARD</ssl_first_name>
 * <ssl_last_name>ELAVONTEST</ssl_last_name>
 * <ssl_card_number>41**********9990</ssl_card_number>
 * <ssl_exp_date>1219</ssl_exp_date>
 * <ssl_entry_mode>S</ssl_entry_mode>
 * <ssl_amount>5.00</ssl_amount>
 * <ssl_result_message>APPROVAL</ssl_result_message>
 * <ssl_approval_code>296197</ssl_approval_code>
 * <ssl_transaction_language>EN</ssl_transaction_language>
 * </txn>
 * <txn>
 * <ssl_txn_id>050218A15-6679F35F-3A37-479A-A4A0-8D971F50B0B9</ssl_txn_id>
 * <ssl_user_id>008382</ssl_user_id>
 * <ssl_trans_status>STL</ssl_trans_status>
 * <ssl_card_type>CREDITCARD</ssl_card_type>
 * <ssl_card_short_description>VISA</ssl_card_short_description>
 * <ssl_transaction_type>SALE</ssl_transaction_type>
 * <ssl_txn_time>02/05/2018 04:39:05 PM</ssl_txn_time>
 * <ssl_card_number>41**********9990</ssl_card_number>
 * <ssl_exp_date>1219</ssl_exp_date>
 * <ssl_entry_mode>S</ssl_entry_mode>
 * <ssl_amount>5.00</ssl_amount>
 * <ssl_result_message>APPROVAL</ssl_result_message>
 * <ssl_approval_code>296020</ssl_approval_code>
 * <ssl_transaction_language>EN</ssl_transaction_language>
 * </txn>
 * <txn>
 * <ssl_txn_id>050218A15-722AEDBF-F290-4458-8221-F9C69FC5904F</ssl_txn_id>
 * <ssl_user_id>008382</ssl_user_id>
 * <ssl_trans_status>STL</ssl_trans_status>
 * <ssl_card_type>CASH</ssl_card_type>
 * <ssl_transaction_type>SALE</ssl_transaction_type>
 * <ssl_txn_time>02/05/2018 03:12:07 PM</ssl_txn_time>
 * <ssl_entry_mode>K</ssl_entry_mode>
 * <ssl_amount>20.00</ssl_amount>
 * <ssl_result_message>APPROVAL</ssl_result_message>
 * <ssl_transaction_language>EN</ssl_transaction_language>
 * </txn>
 * <txn>
 * <ssl_txn_id>050218A15-ADDEB181-568A-471B-B08F-497169990CE4</ssl_txn_id>
 * <ssl_user_id>008382</ssl_user_id>
 * <ssl_trans_status>STL</ssl_trans_status>
 * <ssl_card_type>CASH</ssl_card_type>
 * <ssl_transaction_type>SALE</ssl_transaction_type>
 * <ssl_txn_time>02/05/2018 02:02:16 PM</ssl_txn_time>
 * <ssl_entry_mode>K</ssl_entry_mode>
 * <ssl_amount>12.00</ssl_amount>
 * <ssl_result_message>APPROVAL</ssl_result_message>
 * <ssl_transaction_language>EN</ssl_transaction_language>
 * </txn>
 * <txn>
 * <ssl_txn_id>050218A15-4A399C13-8A34-4DF7-9BBD-C977B5CDE228</ssl_txn_id>
 * <ssl_user_id>008382</ssl_user_id>
 * <ssl_trans_status>STL</ssl_trans_status>
 * <ssl_card_type>CASH</ssl_card_type>
 * <ssl_transaction_type>SALE</ssl_transaction_type>
 * <ssl_txn_time>02/05/2018 02:00:45 PM</ssl_txn_time>
 * <ssl_entry_mode>K</ssl_entry_mode>
 * <ssl_amount>12.00</ssl_amount>
 * <ssl_result_message>APPROVAL</ssl_result_message>
 * <ssl_transaction_language>EN</ssl_transaction_language>
 * </txn>
 * <txn>
 * <ssl_txn_id>050218A15-D835AEBE-0034-4B4E-A2BE-AC9153786045</ssl_txn_id>
 * <ssl_user_id>008382</ssl_user_id>
 * <ssl_trans_status>STL</ssl_trans_status>
 * <ssl_card_type>CASH</ssl_card_type>
 * <ssl_transaction_type>RETURN</ssl_transaction_type>
 * <ssl_txn_time>02/05/2018 12:10:54 AM</ssl_txn_time>
 * <ssl_entry_mode>K</ssl_entry_mode>
 * <ssl_amount>5.00</ssl_amount>
 * <ssl_result_message>APPROVAL</ssl_result_message>
 * <ssl_transaction_language>EN</ssl_transaction_language>
 * </txn>
 * <txn>
 * <ssl_txn_id>050218A15-9FD08B5C-141D-46C2-9AEA-4DF12DFEBA3B</ssl_txn_id>
 * <ssl_user_id>008382</ssl_user_id>
 * <ssl_trans_status>STL</ssl_trans_status>
 * <ssl_card_type>CASH</ssl_card_type>
 * <ssl_transaction_type>SALE</ssl_transaction_type>
 * <ssl_txn_time>02/05/2018 12:10:35 AM</ssl_txn_time>
 * <ssl_entry_mode>K</ssl_entry_mode>
 * <ssl_amount>5.00</ssl_amount>
 * <ssl_result_message>APPROVAL</ssl_result_message>
 * <ssl_transaction_language>EN</ssl_transaction_language>
 * </txn>
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
