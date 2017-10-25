package com.elavon.converge.model;

import com.elavon.converge.xml.XmlMapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class ElavonTransactionTest {

    private XmlMapper xmlMapper;

    @Before
    public void initialize() {
        xmlMapper = new XmlMapper();
    }

    @Test
    public void testRead() throws Exception {
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
        ElavonTransaction transaction = xmlMapper.read(xml, ElavonTransaction.class);

        // VERIFY
        Assert.assertEquals(transaction.getTransactionType(), ElavonTransactionType.SALE);
    }

    @Test
    public void testWriteEnumMapping() throws Exception {
        // SETUP
        ElavonTransaction txn = MockObjectFactory.getElavonTransaction();

        // TEST
        String xml = xmlMapper.write(txn);

        // VERIFY
        Assert.assertNotNull(xml);
        println(xml);
    }


    @Test
    public void testWriteBoolean() throws Exception {
        // SETUP
        ElavonTransaction txn = MockObjectFactory.getElavonTransaction();
        txn.setCardPresent(false);

        // TEST
        String xml = xmlMapper.write(txn);

        // VERIFY
        Assert.assertNotNull(xml);
        println(xml);
    }

    @Test
    public void testWriteBigDecimal() throws Exception {
        //SETUP
        ElavonTransaction txn = MockObjectFactory.getElavonTransaction();
        txn.setSalesTax(new BigDecimal(10.00).setScale(2, BigDecimal.ROUND_HALF_UP));

        // TEST
        String xml = xmlMapper.write(txn);

        // VERIFY
        Assert.assertNotNull(xml);
        println(xml);
    }

    @Test
    public void testWritePartialAuthIndicator() throws Exception {
        // SETUP
        ElavonTransaction txn = MockObjectFactory.getElavonTransaction();
        txn.setPartialAuthIndicator(PartialAuthIndicator.SUPPORTED);

        // TEST
        String xml = xmlMapper.write(txn);

        // VERIFY
        Assert.assertNotNull(xml);
        println(xml);
    }

    private void println(Object o) {
        System.out.println(o);
    }
}
