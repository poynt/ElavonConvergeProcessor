package com.elavon.converge.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;

public class ElavonTransactionTest {

    private Serializer serializer;

    @Before
    public void initialize() {
        serializer = new Persister(new Matcher() {
            @Override
            public Transform match(Class type) throws Exception {
                if (type.isEnum()) {
                    return new EnumTransform(type);
                } else if (type.getSimpleName().equalsIgnoreCase("Boolean")) {
                    return new BooleanTransform(type);
                }
                return null;
            }
        });
    }

    @Test
    public void testWriteEnumMapping() throws Exception{
        ElavonTransactionRequest txn = MockObjectFactory.getElavonTransactionRequest();
        txn.setTransactionType(ElavonTransactionType.SALE);
        txn.setUserId("user");
        txn.setPin("pin");
        txn.setMerchantId("merchant");

        StringWriter writer = new StringWriter();
        serializer.write(txn, writer);
        assertNotNull(writer.toString());
        println(writer.toString());

    }

    @Test
    public void testRead() throws Exception {
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
//            "    <ssl_first_name>Test</ssl_first_name>\n" +
                        "</txn>";
        Reader reader = new StringReader(xml);
        ElavonTransactionRequest transaction = serializer.read(ElavonTransactionRequest.class, reader, false);
        assertEquals(transaction.getTransactionType(), ElavonTransactionType.SALE);
    }

    @Test
    public void testWriteBoolean() throws Exception{
        ElavonTransactionRequest txn = MockObjectFactory.getElavonTransactionRequest();
        txn.setUserId("user");
        txn.setPin("pin");
        txn.setMerchantId("merchant");
        txn.setTransactionType(ElavonTransactionType.SALE);
        txn.setCardPresent(false);

        StringWriter w = new StringWriter();
        serializer.write(txn, w);
        println(w.toString());
    }

    @Test
    public void testWriteBigDecimal() throws Exception {
        ElavonTransactionRequest t = generateTransaction();
        t.setSalesTax(new BigDecimal(10.00).setScale(2, BigDecimal.ROUND_HALF_UP));
        println(t.getSalesTax());
        StringWriter w = new StringWriter();
        serializer.write(t, w);
        System.out.println(w.toString());

        Reader r = new StringReader(w.toString());
        ElavonTransactionRequest transaction = serializer.read(ElavonTransactionRequest.class, r, false);
        println(transaction.getSalesTax());
        assertEquals(t.getSalesTax(), transaction.getSalesTax());

    }

    @Test
    public void testWritePartialAuthIndicator() throws Exception{
        // SETUP
        ElavonTransactionRequest t = generateTransaction();
        StringWriter w = new StringWriter();
        serializer.write(t, w);
        println(w.toString());

        Reader r = new StringReader(w.toString());
        ElavonTransactionRequest transaction = serializer.read(ElavonTransactionRequest.class, r, false);
        println(transaction.getPartialAuthIndicator());
    }

    private ElavonTransactionRequest generateTransaction(){
        ElavonTransactionRequest txn = new ElavonTransactionRequest();
        txn.setUserId("user");
        txn.setPin("pin");
        txn.setMerchantId("merchant");
        txn.setTransactionType(ElavonTransactionType.SALE);
        return txn;
    }

    @Test
    public void testBigDecimalSerialization() throws Exception {
        ElavonTransactionRequest t = MockObjectFactory.getElavonTransactionRequest();
        t.setSalesTax(new BigDecimal(10.00).setScale(2, BigDecimal.ROUND_HALF_UP));
        println(t.getSalesTax());

        StringWriter w = new StringWriter();
        serializer.write(t, w);
        println(w);

        Reader r = new StringReader(w.toString());
        ElavonTransactionRequest transaction = serializer.read(ElavonTransactionRequest.class, r, false);
        println(transaction.getSalesTax());
        assertEquals(t.getSalesTax(), transaction.getSalesTax());
    }

    @Test
    public void testPartialAuthIndicator() throws Exception {
        ElavonTransactionRequest t = MockObjectFactory.getElavonTransactionRequest();
        //t.setPartialAuthIndicator(PartialAuthIndicator.SUPPORTED);
        StringWriter w = new StringWriter();
        serializer.write(t, w);
        println(w);

        Reader r = new StringReader(w.toString());
        ElavonTransactionRequest transaction = serializer.read(ElavonTransactionRequest.class, r, false);
        println(transaction.getPartialAuthIndicator());
    }

    private void println(Object o) {
        System.out.println(o);
    }
}
