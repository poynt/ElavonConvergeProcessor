package com.elavon.converge.model;

import org.junit.Before;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.Assert.assertEquals;

/**
 * Created by dennis on 10/23/17.
 */
public class ElavonTransactionResponseTest {
    Serializer serializer;

    @Before
    public void initialize(){
        serializer = new Persister(new Matcher() {
            @Override
            public Transform match(Class type) throws Exception {
                if (type.isEnum()){
                    return new EnumTransform(type);
                }else if (type.getSimpleName().equalsIgnoreCase("Boolean")){
                    return new BooleanTransform(type);
                }
                return null;
            }
        });

    }

    @Test
    public void testTxnTime() throws Exception{
        ElavonTransactionResponse t = new ElavonTransactionResponse();
        String dateString = "10/23/2017 01:38:45 PM";
        t.setTxnTime(dateString);

        // Elavon date format (time is EST)
        SimpleDateFormat isoFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
        isoFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Date date = isoFormat.parse("10/23/2017 01:38:45 PM");

        Reader r = new StringReader("<txn><ssl_txn_time>" + dateString + "</ssl_txn_time></txn>");
        ElavonTransactionResponse transaction = serializer.read(ElavonTransactionResponse.class, r, false);
        assertEquals(date, transaction.getTxnTimeAsDate());
        println(transaction.getTxnTimeAsDate());
    }

    @Test
    public void testDCCRate() throws Exception{
        float dcc = .7546f;
        Reader r = new StringReader("<txn><ssl_conversion_rate>" + dcc + "</ssl_conversion_rate></txn>");
        ElavonTransactionResponse transaction = serializer.read(ElavonTransactionResponse.class, r, false);
        println(transaction.getConversionRate());
        assertEquals(dcc, transaction.getConversionRate(), 0);
    }

    @Test
    public void testAuth() throws Exception {
        URL url = new URL("https://api.demo.convergepay.com/VirtualMerchantDemo/processxml.do");

        OkHttpClient client = new OkHttpClient();
        MediaType urlEncoded = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

        ElavonTransactionRequest req = new ElavonTransactionRequest();
        req.setMerchantId("009005");
        req.setUserId("devportal");
        req.setPin("BDDZY5KOUDCNPV4L3821K7PETO4Z7TPYOJB06TYBI1CW771IDHXBVBP51HZ6ZANJ");
        req.setTestMode("false");
        req.setTransactionType(ElavonTransactionType.SALE);
        req.setCardNumber("5472063333333330");
        req.setExpDate("1225");
        req.setAmount(new BigDecimal(10.11).setScale(2, BigDecimal.ROUND_HALF_UP));
        req.setFirstName("Rando");
        req.setDescription("tables & chairs");

        StringWriter w = new StringWriter();
        serializer.write(req, w);
        println(w.toString());
        RequestBody body = RequestBody.create(urlEncoded, "xmldata="+ URLEncoder.encode(w.toString(), "UTF-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.toString());
        Reader r = new StringReader(response.body().string());
        ElavonTransactionResponse elavonResponse = serializer.read(ElavonTransactionResponse.class, r, false);
        println("approval code: " + elavonResponse.getApprovalCode());
    }

    private void println(Object o){
        System.out.println(o);
    }
}