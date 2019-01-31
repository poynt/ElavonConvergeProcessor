package com.elavon.converge.model;

import com.elavon.converge.BaseTest;
import com.elavon.converge.xml.XmlMapper;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class ElavonTransactionResponseTest extends BaseTest {

    private XmlMapper xmlMapper;

    @Before
    public void initialize() {
        xmlMapper = new XmlMapper();
    }

    @Test
    public void readTxnTime() throws Exception {
        // SETUP
        String dateString = "10/23/2017 01:38:45 PM";
        // Elavon date format (time is EST)
        SimpleDateFormat isoFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        isoFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = isoFormat.parse("10/23/2017 01:38:45 PM");

        String xml = "<txn><ssl_txn_time>" + dateString + "</ssl_txn_time></txn>";

        // TEST
        ElavonTransactionResponse transaction = xmlMapper.read(xml, ElavonTransactionResponse.class);

        // VERIFY
        assertEquals(date, transaction.getTxnTime());
        print(transaction.getTxnTime());
    }

    @Test
    public void readDCCRate() throws Exception {
        // SETUP
        float dcc = .7546f;
        String xml = "<txn><ssl_conversion_rate>" + dcc + "</ssl_conversion_rate></txn>";

        // TEST
        ElavonTransactionResponse transaction = xmlMapper.read(xml, ElavonTransactionResponse.class);

        // VERIFY
        assertEquals(dcc, transaction.getConversionRate(), 0);
        print(transaction.getConversionRate());
    }

    @Test
    public void readAuthApproval() throws Exception{
        // SETUP
        String xml = "<txn><ssl_card_short_description>MC</ssl_card_short_description><ssl_cvv2_response></ssl_cvv2_response><ssl_account_balance>0.00</ssl_account_balance><ssl_result_message>APPROVAL</ssl_result_message><ssl_invoice_number></ssl_invoice_number><ssl_promo_code></ssl_promo_code><ssl_result>0</ssl_result><ssl_txn_id>271017A15-D11434F9-B6AE-4312-A9BB-034F961636AB</ssl_txn_id><ssl_completion_date></ssl_completion_date><ssl_transaction_type>SALE</ssl_transaction_type><ssl_avs_response> </ssl_avs_response><ssl_account_status></ssl_account_status><ssl_approval_code>CMC648</ssl_approval_code><ssl_enrollment></ssl_enrollment><ssl_exp_date>1225</ssl_exp_date><ssl_loyalty_program></ssl_loyalty_program><ssl_tender_amount></ssl_tender_amount><ssl_departure_date></ssl_departure_date><ssl_card_type>CREDITCARD</ssl_card_type><ssl_loyalty_account_balance></ssl_loyalty_account_balance><ssl_salestax></ssl_salestax><ssl_amount>5.20</ssl_amount><ssl_card_number>54**********3330</ssl_card_number><ssl_issue_points></ssl_issue_points><ssl_txn_time>10/27/2017 01:37:53 PM</ssl_txn_time><ssl_access_code></ssl_access_code></txn>";

        // TEST
        ElavonTransactionResponse transactionResponse = xmlMapper.read(xml, ElavonTransactionResponse.class);
        printJson(transactionResponse);

    }


}
