package com.elavon.converge.model;

import com.elavon.converge.BaseTest;
import com.elavon.converge.xml.XmlMapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class ElavonTransactionRequestTest extends BaseTest {

    private XmlMapper xmlMapper;

    @Before
    public void initialize() {
        xmlMapper = new XmlMapper();
    }

    @Test
    public void read() throws Exception {
        // SETUP
        String xml =
                "<txn>\n" +
                        "    <ssl_merchant_id>my_virtualmerchant_id</ssl_merchant_id>\n" +
                        "    <ssl_user_id>my_user</ssl_user_id>\n" +
                        "    <ssl_pin>my_pin</ssl_pin>\n" +
                        "    <ssl_test_mode>false</ssl_test_mode>\n" +
                        "    <ssl_transaction_type>ccsale</ssl_transaction_type>\n" +
                        "    <ssl_card_number>00*********0000</ssl_card_number>\n" +
                        "    <ssl_exp_date>1215</ssl_exp_date>\n" +
                        "    <ssl_amount>10.00</ssl_amount>\n" +
                        "    <ssl_cvv2cvc2_indicator>1</ssl_cvv2cvc2_indicator>\n" +
                        "    <ssl_cvv2cvc2>123</ssl_cvv2cvc2>\n" +
                        "    <ssl_first_name>Test</ssl_first_name>\n" +
                        "</txn>";

        // TEST
        ElavonTransactionRequest transaction = xmlMapper.read(xml, ElavonTransactionRequest.class);

        // VERIFY
        Assert.assertEquals(transaction.getTransactionType(), ElavonTransactionType.SALE);
    }

    @Test
    public void writeEnumMapping() throws Exception {
        // SETUP
        ElavonTransactionRequest txn = MockObjectFactory.getElavonTransactionRequest();

        // TEST
        String xml = xmlMapper.write(txn);

        // VERIFY
        Assert.assertNotNull(xml);
        print(xml);
    }


    @Test
    public void writeBoolean() throws Exception {
        // SETUP
        ElavonTransactionRequest txn = MockObjectFactory.getElavonTransactionRequest();
        txn.setCardPresent(false);

        // TEST
        String xml = xmlMapper.write(txn);

        // VERIFY
        Assert.assertNotNull(xml);
        print(xml);
    }

    @Test
    public void writeBigDecimal() throws Exception {
        //SETUP
        ElavonTransactionRequest txn = MockObjectFactory.getElavonTransactionRequest();
        txn.setSalesTax(new BigDecimal(10.00).setScale(2, BigDecimal.ROUND_HALF_UP));

        // TEST
        String xml = xmlMapper.write(txn);

        // VERIFY
        Assert.assertNotNull(xml);
        print(xml);
    }

    @Test
    public void writePartialAuthIndicator() throws Exception {
        // SETUP
        ElavonTransactionRequest txn = MockObjectFactory.getElavonTransactionRequest();
        txn.setPartialAuthIndicator(PartialAuthIndicator.SUPPORTED);

        // TEST
        String xml = xmlMapper.write(txn);

        // VERIFY
        Assert.assertNotNull(xml);
        print(xml);
    }
}
