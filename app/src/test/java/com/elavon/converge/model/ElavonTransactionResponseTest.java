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
        assertEquals(date, transaction.getTxnTimeAsDate());
        print(transaction.getTxnTimeAsDate());
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
}
