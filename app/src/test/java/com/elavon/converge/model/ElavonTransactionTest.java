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


/**
 * Created by dennis on 10/23/17.
 */
public class ElavonTransactionTest {

    Serializer serializer;

    @Before
    public void initialize(){
        serializer = new Persister(new Matcher() {
            @Override
            public Transform match(Class type) throws Exception {
                if (type.isEnum()){
                    return new EnumTransform(type);
                }
                return null;
            }
        });
    }
    @Test
    public void testEnumMapping(){


        try {
            ElavonTransaction txn = new ElavonTransaction();
            txn.setTransactionType(ElavonTransactionType.SALE);
            txn.setUserId("user");
            txn.setPin("pin");
            txn.setMerchantId("merchant");

            StringWriter writer = new StringWriter();
            serializer.write(txn, writer);
            assertNotNull(writer.toString());
            System.out.println(writer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeserialization(){
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
        try {
            ElavonTransaction transaction = serializer.read(ElavonTransaction.class, reader, false);
            assertEquals(transaction.getTransactionType(), ElavonTransactionType.SALE);
        } catch (Exception e) {
            e.printStackTrace();
            assertNull(e);
        }

    }
}