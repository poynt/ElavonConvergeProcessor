package com.elavon.converge.processor;

import android.util.Log;

import com.elavon.converge.exception.ConvergeClientException;
import com.elavon.converge.model.ElavonSettleResponse;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.ElavonTransactionSearchRequest;
import com.elavon.converge.model.ElavonTransactionSearchResponse;
import com.elavon.converge.model.mapper.ConvergeMapper;

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
            Log.e(TAG, "Max retry count reached. Starting reversal...");
            callback.onFailure(
                    new ConvergeClientException("Transaction failed after all retries",
                            true /* network error */));
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
                    //Log.e(TAG, "Create transaction timeout. Retrying...");
                    //checkAndCreate(request, callback, retryCount + 1, originalTime);
                    //return;
                    callback.onFailure(
                            new ConvergeClientException("Transaction failed due to network error",
                                    true /* network error */));
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
        find(request.getMerchantTxnId(), originalTime, cb);
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

    public void get(
            final String transactionId,
            final ConvergeCallback<ElavonTransactionResponse> callback) {
        final ElavonTransactionSearchRequest searchRequest = convergeMapper.getSearchRequest(transactionId);
        convergeClient.call(searchRequest, callback);
    }

    public void find(
            final String merchantTransactionId,
            final Date dateAfter,
            final ConvergeCallback<ElavonTransactionResponse> callback) {
        if (merchantTransactionId == null) {
            callback.onFailure(new ConvergeClientException("Transaction not found. Not enough information."));
            return;
        }

        final ConvergeCallback cb = new ConvergeCallback<ElavonTransactionSearchResponse>() {
            @Override
            public void onResponse(final ElavonTransactionSearchResponse response) {
                // find the matching transaction object
                Log.i(TAG, "Found transaction for merchantTransactionId:" + merchantTransactionId);
                if (response.getList() != null) {
                    for (final ElavonTransactionResponse tr : response.getList()) {
                        Log.d(TAG, tr.toString());
                        callback.onResponse(tr);
                    }
                }
                callback.onFailure(new ConvergeClientException("Transaction not found"));
            }

            @Override
            public void onFailure(final Throwable t) {
                callback.onFailure(t);
            }
        };

        final ElavonTransactionSearchRequest searchRequest
                = convergeMapper.getSearchByMerchantTransactionIdRequest(merchantTransactionId, dateAfter);
        convergeClient.call(searchRequest, cb);
    }

    /**
     * Get all transactions in the given date/time range
     * Start/End date of the search window. Format MM/DD/YYYY or MM/DD/YYYY hh:mm:ss AM or
     * MM/DD/YYYY hh:mm:ss PM. Example: 04/28/2015 08:00:34 AM, 04/28/2015
     *
     * @param dateAfter  Example: 04/28/2015 08:00:34 AM, 04/28/2015
     * @param dateBefore Example: 04/29/2015 11:59:59 PM
     * @param callback
     */
    public void fetchTransactions(
            final String dateAfter,
            final String dateBefore,
            final ConvergeCallback<ElavonTransactionSearchResponse> callback) {
        // we need either start or end - converge searches accordingly
        // if only startDate given, converge searches +30 days
        // if only endDate given, converge searches -30 days
        if (dateBefore == null && dateAfter == null) {
            callback.onFailure(new ConvergeClientException("Transaction not found. Not enough information."));
            return;
        }

        final ElavonTransactionSearchRequest searchRequest = convergeMapper.getSearchRequest(dateAfter, dateBefore);
        convergeClient.call(searchRequest, callback);
    }

    public void fetchTransactions(
            final String transactionType,
            final String dateAfter,
            final String dateBefore,
            final ConvergeCallback<ElavonTransactionSearchResponse> callback) {
        // we need either start or end - converge searches accordingly
        // if only startDate given, converge searches +30 days
        // if only endDate given, converge searches -30 days
        if (transactionType == null || (dateBefore == null && dateAfter == null)) {
            callback.onFailure(new ConvergeClientException("Transaction not found. Not enough information."));
            return;
        }

        final ElavonTransactionSearchRequest searchRequest = convergeMapper.getSearchRequest(transactionType, dateAfter, dateBefore);
        convergeClient.call(searchRequest, callback);
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
