package com.elavon.converge.processor;

import com.elavon.converge.exception.ConvergeClientException;
import com.elavon.converge.model.ElavonRequest;
import com.elavon.converge.model.ElavonResponse;
import com.elavon.converge.xml.XmlMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConvergeClient {

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int WRITE_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 5000;
    private static final MediaType FORM_URL_ENCODED_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private final String host;
    private final OkHttpClient client;
    private final XmlMapper xmlMapper;

    public ConvergeClient() {
        // TODO have these injected
        host = "https://api.demo.convergepay.com/VirtualMerchantDemo/processxml.do";
        client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build();
        xmlMapper = new XmlMapper();
    }

    public <T extends ElavonResponse> T callSync(final ElavonRequest model, final Class<T> responseClass) {
        final Request request = new Request.Builder()
                .url(host)
                .post(RequestBody.create(FORM_URL_ENCODED_TYPE, getFormUrlencodedXml(model)))
                .build();
        try {
            final Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return getResponse(response.body().string(), responseClass);
            } else {
                throw new ConvergeClientException("Call not successful. Message: " + response.message());
            }
        } catch (IOException e) {
            throw new ConvergeClientException("Call not successful", e);
        }
    }

    private String getFormUrlencodedXml(final ElavonRequest model) {
        try {
            return "xmldata=" + URLEncoder.encode(xmlMapper.write(model), "UTF-8");
        } catch (Exception e) {
            throw new ConvergeClientException("Invalid XML request", e);
        }
    }

    private <T extends ElavonResponse> T getResponse(final String xml, final Class<T> responseClass) {
        try {
            return xmlMapper.read(xml, responseClass);
        } catch (Exception e) {
            throw new ConvergeClientException("Invalid XML response", e);
        }
    }
}
