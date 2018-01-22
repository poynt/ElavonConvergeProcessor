package com.elavon.converge.processor;

import android.util.Log;

import com.elavon.converge.exception.ConvergeClientException;
import com.elavon.converge.model.ElavonSettleResponse;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.ElavonTransactionSearchRequest;
import com.elavon.converge.model.ElavonTransactionSearchResponse;
import com.elavon.converge.model.mapper.ConvergeMapper;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class ConvergeService {

    private static final String TAG = "ConvergeService";

    protected final ConvergeMapper convergeMapper;
    protected final ConvergeClient convergeClient;
    protected final int maxRetryCount;

    @Inject
    public ConvergeService(final ConvergeMapper convergeMapper, final ConvergeClient convergeClient, final int maxRetryCount) {
        this.convergeMapper = convergeMapper;
        this.convergeClient = convergeClient;
        this.maxRetryCount = maxRetryCount;
    }

    public void create(final ElavonTransactionRequest request, final ConvergeCallback<ElavonTransactionResponse> callback) {
        create(request, callback, 0, new Date());
    }

    private void create(
            final ElavonTransactionRequest request,
            final ConvergeCallback<ElavonTransactionResponse> callback,
            final int retryCount,
            final Date originalTime) {

        if (retryCount > maxRetryCount) {
            // TODO submit reversal
            Log.e(TAG, "Max retry count reached. Starting reversal...");
            callback.onFailure(new ConvergeClientException("Transaction failed after all retries"));
            return;
        }
        Log.i(TAG, "Create with retry count: " + retryCount);

        final ConvergeCallback cb = new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse response) {
                callback.onResponse(response);
            }

            @Override
            public void onFailure(final Throwable t) {
                // retry if timeout
                if (t instanceof SocketTimeoutException) {
                    Log.e(TAG, "Create transaction timeout. Retrying...");
                    checkAndCreate(request, callback, retryCount + 1, originalTime);
                    return;
                }
                callback.onFailure(t);
            }
        };
        convergeClient.call(request, cb);
    }

    private void checkAndCreate(
            final ElavonTransactionRequest request,
            final ConvergeCallback<ElavonTransactionResponse> callback,
            final int retryCount,
            final Date originalTime) {
        final ConvergeCallback cb = new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse response) {
                Log.i(TAG, "Found transaction after timeout");
                callback.onResponse(response);
            }

            @Override
            public void onFailure(final Throwable t) {
                Log.e(TAG, "Failed to find transaction after timeout");
                create(request, callback, retryCount, originalTime);
            }
        };

        find(request.getCardLast4(), request.getAmount(), originalTime, cb);
    }

    public void update(
            final ElavonTransactionRequest request,
            final ConvergeCallback<ElavonTransactionResponse> callback) {
        convergeClient.call(request, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse response) {
                callback.onResponse(response);
            }

            @Override
            public void onFailure(final Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public void find(
            final String cardLast4,
            final BigDecimal amount,
            final Date dateAfter,
            final ConvergeCallback<ElavonTransactionResponse> callback) {
        if (cardLast4 == null || cardLast4.length() < 4 || amount == null || dateAfter == null) {
            callback.onFailure(new ConvergeClientException("Transaction not found. Not enough information."));
            return;
        }

        final ConvergeCallback cb = new ConvergeCallback<ElavonTransactionSearchResponse>() {
            @Override
            public void onResponse(final ElavonTransactionSearchResponse response) {
                // find the matching transaction object
                if (response.getList() != null) {
                    for (final ElavonTransactionResponse tr : response.getList()) {
                        if (amount.equals(tr.getAmount())) {
                            callback.onResponse(tr);
                            return;
                        }
                    }
                }
                callback.onFailure(new ConvergeClientException("Transaction not found"));
            }

            @Override
            public void onFailure(final Throwable t) {
                callback.onFailure(t);
            }
        };

        final ElavonTransactionSearchRequest searchRequest = convergeMapper.getSearchRequest(cardLast4, dateAfter);
        convergeClient.call(searchRequest, cb);
    }

    public void generateToken(final String cardNumber, final String expiry, final ConvergeCallback<ElavonTransactionResponse> callback) {
        convergeClient.call(convergeMapper.getGenerateTokenRequest(cardNumber, expiry), callback);
    }

    /**
     * Settle all or only the transaction ids given
     *
     * @param transactionIds list of Converge transaction ids
     * @param callback
     */
    public void settle(final List<String> transactionIds, final ConvergeCallback<ElavonSettleResponse> callback) {
        convergeClient.call(convergeMapper.getSettleRequest(transactionIds), callback);
    }
}
