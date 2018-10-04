package com.elavon.converge.processor;

import android.text.TextUtils;
import android.util.Log;

import com.elavon.converge.exception.ConvergeClientException;
import com.elavon.converge.model.ElavonRequest;
import com.elavon.converge.model.ElavonResponse;
import com.elavon.converge.xml.XmlMapper;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.URLEncoder;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConvergeClient {

    private static final MediaType FORM_URL_ENCODED_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static final String TAG = ConvergeClient.class.getName();

    protected String merchantId;
    protected String userId;
    protected String pin;
    protected final String host;
    protected final OkHttpClient client;
    protected final XmlMapper xmlMapper;

    @Inject
    public ConvergeClient(
            final String merchantId,
            final String userId,
            final String pin,
            final String host,
            final OkHttpClient client,
            final XmlMapper xmlMapper) {
        this.merchantId = merchantId;
        this.userId = userId;
        this.pin = pin;
        this.host = host;
        this.client = client;
        this.xmlMapper = xmlMapper;
    }

    public void updateCredentials(
            final String merchantId,
            final String userId,
            final String pin
    ) {
        Log.d(TAG, "Updating Converge Credentials with mechantId: " + merchantId + " userId: " + userId + "and pin: " + pin);
        this.merchantId = merchantId;
        this.userId = userId;
        this.pin = pin;
    }

    public <T extends ElavonResponse> void call(final ElavonRequest model, final ConvergeCallback<T> callback) {

        try {
            final Request request = getRequest(model);
            final Callback cb = new Callback() {
                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final T body;
                        try {
                            final String xml = response.body().string();
                            final Class<T> clazz = ((Class) ((ParameterizedType) callback.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
                            body = getResponse(xml, clazz);
                        } catch (final Exception e) {
                            callback.onFailure(e);
                            return;
                        }
                        callback.onResponse(body);
                    } else {
                        callback.onFailure(new ConvergeClientException("Failed request with http status code: " + response.code()));
                    }
                }

                @Override
                public void onFailure(final Call call, final IOException e) {
                    callback.onFailure(e);
                }
            };
            client.newCall(request).enqueue(cb);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            callback.onFailure(new ConvergeClientException("Failed request with exception." + e.getMessage()));
        }
    }

    public <T extends ElavonResponse> T callSync(final ElavonRequest model, final Class<T> responseClass) {

        try {
            final Request request = getRequest(model);
            final Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return getResponse(response.body().string(), responseClass);
            } else {
                throw new ConvergeClientException("Call not successful. Message: " + response.message());
            }
        } catch (Exception e) {
            throw new ConvergeClientException("Call not successful", e);
        }
    }

    private Request getRequest(final ElavonRequest request) {
        try {
            if(!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(merchantId) && !TextUtils.isEmpty(pin)) {
                // set credentials
                request.setMerchantId(merchantId);
                Log.d(TAG, "set request merchantId to : " + merchantId);
                request.setUserId(userId);
                Log.d(TAG, "set request pin to: " + pin);
                request.setPin(pin);
                request.setVendorId("POYNT000");
                return new Request.Builder()
                        .url(host)
                        .post(RequestBody.create(FORM_URL_ENCODED_TYPE, "xmldata=" + URLEncoder.encode(xmlMapper.write(request), "UTF-8")))
                        .build();
            } else {
                throw new ConvergeClientException("Credentials data not updated from cloud, can't transact.", null);
            }
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
