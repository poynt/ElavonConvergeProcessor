package com.elavon.converge.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.elavon.converge.exception.ConvergeClientException;
import com.elavon.converge.model.ElavonSettleResponse;
import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.mapper.ConvergeMapper;
import com.elavon.converge.model.type.ElavonTransactionType;
import com.elavon.converge.processor.ConvergeCallback;
import com.elavon.converge.processor.ConvergeService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSourceEntryDetails;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAction;
import co.poynt.api.model.TransactionStatus;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntTransactionBalanceInquiryListener;
import co.poynt.os.services.v1.IPoyntTransactionService;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class TransactionManager {

    private static final String TAG = "TransactionManager";
    private static final int CACHE_SIZE = 100; // 100 transactions

    private IPoyntTransactionService mPoyntTransactionService;

    protected Context context;
    protected ConvergeService convergeService;
    protected ConvergeMapper convergeMapper;
    private LruCache<String, Transaction> transactionCache;
    private LruCache<String, EMVData> emvDataCache;

    @Inject
    public TransactionManager(final Context context,
                              final ConvergeService convergeService,
                              final ConvergeMapper convergeMapper) {
        this.context = context;
        this.convergeService = convergeService;
        this.convergeMapper = convergeMapper;

        transactionCache = new LruCache<>(CACHE_SIZE);
        emvDataCache = new LruCache<>(CACHE_SIZE);
        bindToPoyntServices();
    }

    public void bindToPoyntServices() {
        context.bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_TRANSACTION_SERVICE),
                mTransactionServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void disconnectFromPoyntServices() {
        context.unbindService(mTransactionServiceConnection);
    }

    /**
     * Class for interacting with poynt transaction service
     */
    private final ServiceConnection mTransactionServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mPoyntTransactionService = IPoyntTransactionService.Stub.asInterface(iBinder);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mPoyntTransactionService = null;
            bindToPoyntServices();
        }
    };

    public void processTransaction(final Transaction transaction,
                                   final String requestId,
                                   final IPoyntTransactionServiceListener listener) throws RemoteException {
        Log.d(TAG, "processTransaction");
        final Date transactionInitiationTime = new Date();
        // if the transaction action is REFUND and if it has a parentId - let's get it first
        if (transaction.getAction() == TransactionAction.REFUND) {

            // if it's a regular refund - get parent transaction from poynt service first
            // if it's non-ref refund just send refund request
            if (transaction.getParentId() != null) {
                getTransaction(transaction.getParentId().toString(),
                        requestId,
                        new IPoyntTransactionServiceListener.Stub() {
                            @Override
                            public void onResponse(final Transaction parentTransaction,
                                                   final String requestId,
                                                   final PoyntError poyntError) throws RemoteException {
                                refundTransaction(transaction,
                                        parentTransaction,
                                        requestId,
                                        listener);
                            }

                            @Override
                            public void onLoginRequired() throws RemoteException {
                                if (listener != null) {
                                    listener.onLoginRequired();
                                }
                            }

                            @Override
                            public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                                if (listener != null) {
                                    listener.onLaunchActivity(intent, s);
                                }
                            }
                        });
            } else {
                refundTransaction(transaction, null, requestId, listener);
            }
        } else {
            final ElavonTransactionRequest request = convergeMapper.getTransactionRequest(transaction);

            convergeService.create(request, new ConvergeCallback<ElavonTransactionResponse>() {
                @Override
                public void onResponse(final ElavonTransactionResponse elavonResponse) {
                    try {
                        convergeMapper.mapTransactionResponse(elavonResponse, transaction);
                        transactionCache.put(transaction.getId().toString(), transaction);
                        listener.onResponse(transaction, requestId, null);
                    } catch (final RemoteException e) {
                        Log.e(TAG, "Failed to respond", e);
                    }
                }

                @Override
                public void onFailure(final Throwable t) {
                    Log.e(TAG, "Transaction processing failure", t);
                    PoyntError error = new PoyntError(PoyntError.CODE_PROCESSOR_UNRESPONSIVE);
                    if (t instanceof ConvergeClientException) {
                        if (((ConvergeClientException) t).isNetworkError()) {
                            // we must reverse the transaction just in case
                            error = new PoyntError(PoyntError.CODE_NETWORK_ERROR);
                            // start reversal after 15secs as per John's recommendation
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    findAndReverseTransaction(transactionInitiationTime,
                                            request.getMerchantTxnId());
                                }
                            }, 15000);
                        }
                    }
                    error.setThrowable(t);
                    try {
                        listener.onResponse(transaction, requestId, error);
                    } catch (final RemoteException e) {
                        Log.e(TAG, "Failed to respond", e);
                    }
                }
            });
        }
    }

    private void findAndReverseTransaction(final Date transactionInitiationTime,
                                           final String merchantTransactionId) {
        final ConvergeCallback cb = new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse response) {
                Log.i(TAG, "Found transaction after timeout");
                // reverse transaction
                Log.d(TAG, "merchant transaction id: " + merchantTransactionId
                        + " converge txn_id:" + response.getTxnId());
                ElavonTransactionType reversalTransactionType = ElavonTransactionType.DELETE;
//                switch (response.getTransactionType()) {
//                    case SALE:
//                        reversalTransactionType = ElavonTransactionType.DELETE;
//                        break;
//                    case AUTH_ONLY:
//                        reversalTransactionType = ElavonTransactionType.DELETE;
//                        break;
//                    case EMV_CT_AUTH_ONLY:
//                    case EMV_CT_SALE:
//                    case EMV_SWIPE_AUTH_ONLY:
//                    case EMV_SWIPE_SALE:
//                        reversalTransactionType = ElavonTransactionType.EMV_REVERSAL;
//                        break;
//                }
                final ElavonTransactionRequest request = new ElavonTransactionRequest();
                request.setTransactionType(reversalTransactionType);
                // elavon transactionId
                request.setTxnId(response.getTxnId());
                convergeService.update(request, new ConvergeCallback<ElavonTransactionResponse>() {
                    @Override
                    public void onResponse(final ElavonTransactionResponse elavonResponse) {
                        Log.d(TAG, elavonResponse != null ? elavonResponse.toString() : "n/a");
                        if (elavonResponse.isSuccess()) {
                            Log.i(TAG, "reverseTransaction: " + response.getTxnId() + " SUCCESS");
                        } else {
                            Log.e(TAG, "reverseTransaction: " + response.getTxnId() + " FAILED");
                        }
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        t.printStackTrace();
                        Log.e(TAG, "reverseTransaction: " + response.getTxnId() + " FAILED");
                    }
                });
            }

            @Override
            public void onFailure(final Throwable t) {
                Log.e(TAG, "Failed to find transaction after timeout");
            }
        };

        convergeService.find(merchantTransactionId, transactionInitiationTime, cb);
    }

    public void captureTransaction(
            final String transactionId,
            final AdjustTransactionRequest adjustTransactionRequest,
            final String requestId,
            final IPoyntTransactionServiceListener listener) throws RemoteException {
        Log.d(TAG, "captureTransaction: " + transactionId);
        // get transaction
        getTransaction(transactionId, requestId, new IPoyntTransactionServiceListener.Stub() {
            @Override
            public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {

                final ElavonTransactionRequest request = convergeMapper.getTransactionCompleteRequest(
                        transaction.getFundingSource(),
                        transaction.getProcessorResponse().getRetrievalRefNum(),
                        adjustTransactionRequest);
                convergeService.update(request, new ConvergeCallback<ElavonTransactionResponse>() {
                    @Override
                    public void onResponse(final ElavonTransactionResponse elavonResponse) {
                        try {
                            if (elavonResponse.isSuccess()) {
                                transaction.setAction(TransactionAction.CAPTURE);
                                transaction.setAmounts(adjustTransactionRequest.getAmounts());
                                convergeMapper.mapTransactionResponse(elavonResponse, transaction);
                                listener.onResponse(transaction, requestId, null);
                            } else {
                                listener.onResponse(transaction, requestId, new PoyntError(PoyntError.CODE_API_ERROR));
                            }
                        } catch (final RemoteException e) {
                            Log.e(TAG, "Failed to respond", e);
                        }
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        final PoyntError error = new PoyntError(PoyntError.CODE_API_ERROR);
                        error.setThrowable(t);
                        try {
                            listener.onResponse(transaction, requestId, error);
                        } catch (final RemoteException e) {
                            Log.e(TAG, "Failed to respond", e);
                        }
                    }
                });
            }

            @Override
            public void onLoginRequired() throws RemoteException {
                if (listener != null) {
                    listener.onLoginRequired();
                }
            }

            @Override
            public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                if (listener != null) {
                    listener.onLaunchActivity(intent, s);
                }
            }
        });
    }

    public void updateTransaction(
            final String transactionId,
            final AdjustTransactionRequest adjustTransactionRequest,
            final String requestId,
            final IPoyntTransactionServiceListener listener) throws RemoteException {
        Log.d(TAG, "updateTransaction: " + transactionId);

        // check if we have emvData from captureEmvData()
        if (adjustTransactionRequest.getEmvData() == null) {
            EMVData emvData = emvDataCache.get(transactionId);
            if (emvData != null) {
                Log.d(TAG, "Found emvdata from cache for the same transactionIs - using it.");
                adjustTransactionRequest.setEmvData(emvData);
                // remove from the cache
                emvDataCache.remove(transactionId);
            }
        } else {
            // remove from the cache if any for the transactionId
            emvDataCache.remove(transactionId);
        }

        // get transaction from poynt service first
        getTransaction(transactionId, requestId, new IPoyntTransactionServiceListener.Stub() {
            @Override
            public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {

                // nothing to update for debit
                if (Boolean.TRUE.equals(transaction.getFundingSource().isDebit())) {
                    listener.onResponse(transaction, requestId, null);
                    return;
                }

                // update in converge
                // if there is tip - call update tip API followed by signature
                boolean signatureOnlyForMSR = false;
                FundingSourceEntryDetails entryDetails = transaction.getFundingSource().getEntryDetails();
                if (entryDetails != null
                        && entryDetails.getEntryMode() == EntryMode.TRACK_DATA_FROM_MAGSTRIPE) {

                    if (adjustTransactionRequest.getAmounts() == null
                            && adjustTransactionRequest.getSignature() != null) {
                        // if there is no amounts but signature then only update signature
                        signatureOnlyForMSR = true;
                    } else if (adjustTransactionRequest.getAmounts().isCustomerOptedNoTip() != null
                            && adjustTransactionRequest.getAmounts().isCustomerOptedNoTip() == Boolean.TRUE
                            && adjustTransactionRequest.getSignature() != null) {
                        // if there is amounts but customer opted no tip and there is signature
                        signatureOnlyForMSR = true;
                    } else {
                        // if there is tip amount and signature - we let the tip update happen first
                        // followed by signature from the onResponse below
                    }
                }
                if (!signatureOnlyForMSR) {
                    final ElavonTransactionRequest request = convergeMapper.getTransactionUpdateRequest(
                            transaction.getFundingSource().getEntryDetails(),
                            transaction.getProcessorResponse().getRetrievalRefNum(),
                            adjustTransactionRequest);
                    convergeService.update(request, new ConvergeCallback<ElavonTransactionResponse>() {
                        @Override
                        public void onResponse(final ElavonTransactionResponse elavonResponse) {
                            try {
                                if (elavonResponse.isSuccess()) {
                                    // if it's MSR and if we have signature it's a separate call
                                    if (transaction.getFundingSource().getEntryDetails().getEntryMode()
                                            == EntryMode.TRACK_DATA_FROM_MAGSTRIPE
                                            && adjustTransactionRequest.getSignature() != null) {
                                        updateSignature(transaction, adjustTransactionRequest, requestId, listener);
                                    } else {
                                        listener.onResponse(transaction, requestId, null);
                                    }
                                } else {
                                    Log.e(TAG, "Failed to update tip/emv tags");
                                    // if the original request also has signature - we should try saving it
                                    // if it's MSR and if we have signature it's a separate call
                                    if (transaction.getFundingSource().getEntryDetails().getEntryMode()
                                            == EntryMode.TRACK_DATA_FROM_MAGSTRIPE
                                            && adjustTransactionRequest.getSignature() != null) {
                                        Log.i(TAG, "Saving signature");
                                        updateSignature(transaction, adjustTransactionRequest, requestId, listener);
                                    } else {
                                        listener.onResponse(transaction, requestId, new PoyntError(PoyntError.CODE_API_ERROR));
                                    }
                                }
                            } catch (final RemoteException e) {
                                Log.e(TAG, "Failed to respond", e);
                            }
                        }

                        @Override
                        public void onFailure(final Throwable t) {
                            final PoyntError error = new PoyntError(PoyntError.CODE_API_ERROR);
                            error.setThrowable(t);
                            try {
                                listener.onResponse(transaction, requestId, error);
                            } catch (final RemoteException e) {
                                Log.e(TAG, "Failed to respond", e);
                            }
                        }
                    });
                } else {
                    Log.i(TAG, "Saving signature only");
                    updateSignature(transaction, adjustTransactionRequest, requestId, listener);
                }
            }

            @Override
            public void onLoginRequired() throws RemoteException {
                if (listener != null) {
                    listener.onLoginRequired();
                }
            }

            @Override
            public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                if (listener != null) {
                    listener.onLaunchActivity(intent, s);
                }
            }
        });
    }

    public void updateSignature(final Transaction transaction,
                                final AdjustTransactionRequest adjustTransactionRequest,
                                final String requestId,
                                final IPoyntTransactionServiceListener listener) throws RemoteException {
        Log.d(TAG, "updateSignature: " + transaction.getId());
        // if it's MSR - signature goes in as a separate ccsignature request
        final ElavonTransactionRequest request = convergeMapper.getUpdateSignatureRequest(
                transaction.getFundingSource().getEntryDetails(),
                transaction.getProcessorResponse().getRetrievalRefNum(),
                adjustTransactionRequest);
        convergeService.update(request, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse elavonResponse) {
                try {
                    if (elavonResponse.isSuccess()) {
                        listener.onResponse(transaction, requestId, null);
                    } else {
                        listener.onResponse(transaction, requestId, new PoyntError(PoyntError.CODE_API_ERROR));
                    }
                } catch (final RemoteException e) {
                    Log.e(TAG, "Failed to respond", e);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                final PoyntError error = new PoyntError(PoyntError.CODE_API_ERROR);
                error.setThrowable(t);
                try {
                    listener.onResponse(transaction, requestId, error);
                } catch (final RemoteException e) {
                    Log.e(TAG, "Failed to respond", e);
                }
            }
        });
    }

    /**
     * Record EMV data from Card after completing a transaction
     *
     * @param transactionId
     * @param emvData
     * @param requestId
     */
    public void captureEMVData(
            final String transactionId,
            final EMVData emvData,
            final String requestId) throws RemoteException {
        Log.d(TAG, "captureEMVData: " + transactionId);

        // store the emvData in cache in case if we receive
        emvDataCache.put(transactionId, emvData);

        // start background thread to check if the emvData has been sent to server or not
        // after 60 secs
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                // check if the emvData has been processed or not
                final EMVData emvData1 = emvDataCache.get(transactionId);
                if (emvData1 != null) {
                    emvDataCache.remove(transactionId);
                    // get transaction from poynt service first
                    try {
                        getTransaction(transactionId, requestId, new IPoyntTransactionServiceListener.Stub() {
                            @Override
                            public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {
                                // update in converge
                                // let's create an AdjustTransactionRequest so we don't need to create multiple variations
                                // of this method
                                AdjustTransactionRequest adjustTransactionRequest = new AdjustTransactionRequest();
                                adjustTransactionRequest.setEmvData(emvData1);

                                final ElavonTransactionRequest request = convergeMapper.getTransactionUpdateRequest(
                                        transaction.getFundingSource().getEntryDetails(),
                                        transaction.getProcessorResponse().getRetrievalRefNum(),
                                        adjustTransactionRequest);
                                convergeService.update(request, new ConvergeCallback<ElavonTransactionResponse>() {
                                    @Override
                                    public void onResponse(final ElavonTransactionResponse elavonResponse) {
                                        if (elavonResponse.isSuccess()) {
                                            Log.d(TAG, "Successfully Captured EMV Data for: " + transaction.getId());
                                        } else {
                                            Log.e(TAG, "Failed to capture EMV Data for: " + transaction.getId());
                                        }
                                    }

                                    @Override
                                    public void onFailure(final Throwable t) {
                                        Log.e(TAG, "Failed to capture EMV Data for: " + transaction.getId());
                                    }
                                });
                            }

                            @Override
                            public void onLoginRequired() throws RemoteException {
                                Log.e(TAG, "Failed to get transaction:" + transactionId);
                            }

                            @Override
                            public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                                Log.e(TAG, "Failed to get transaction:" + transactionId);
                            }
                        });
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "EMVData already captured for :" + transactionId);
                }
            }
        }, 60 * 1000);


    }

    public void voidTransaction(final String transactionId,
                                EMVData emvData,
                                final String requestId,
                                final IPoyntTransactionServiceListener listener) throws RemoteException {

        Log.d(TAG, "voidTransaction: " + transactionId);

        // get transaction from poynt service first
        getTransaction(transactionId,
                requestId,
                new IPoyntTransactionServiceListener.Stub() {
                    @Override
                    public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {

                        // update action to VOID so when we return the voided transaction object
                        // it has correct status
                        transaction.setAction(TransactionAction.VOID);
                        String trxnId = transaction.getProcessorResponse().getRetrievalRefNum();
                        if(TextUtils.isEmpty(trxnId)) {
                            trxnId = transaction.getProcessorResponse().getTransactionId();
                        }

                        final ElavonTransactionRequest request = convergeMapper.getTransactionVoidRequest(
                                transaction, trxnId);
                        convergeService.update(request, new ConvergeCallback<ElavonTransactionResponse>() {
                            @Override
                            public void onResponse(final ElavonTransactionResponse elavonResponse) {
                                Log.d(TAG, elavonResponse != null ? elavonResponse.toString() : "n/a");
                                if (elavonResponse.isSuccess() || elavonResponse.getErrorCode() == 5040) { // Because 5040 means txn is already voided. Typically happens when txn was voided from web and the sync hasn't happened yet. So we need to send a false success.
                                    Log.i(TAG, "voidTransaction: " + transaction.getId() + " SUCCESS");
                                    if (listener != null) {
                                        convergeMapper.mapTransactionResponse(elavonResponse, transaction);
                                        try {
                                            // update the transactionId w/ new void txn Id and set parent
                                            transaction.setAction(TransactionAction.REFUND);
                                            transaction.setActionVoid(true);
                                            transaction.setParentId(transaction.getId());
                                            transaction.setId(UUID.randomUUID());
                                            listener.onResponse(transaction, requestId, null);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    Log.e(TAG, "voidTransaction: " + transaction.getId() + " FAILED");
                                    if (listener != null) {
                                        try {
                                            // TODO need better error mapping
                                            PoyntError error = new PoyntError(PoyntError.CODE_API_ERROR);
                                            listener.onResponse(transaction, requestId, error);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(final Throwable t) {
                                t.printStackTrace();
                                Log.e(TAG, "voidTransaction: " + transaction.getId() + " FAILED");
                                if (listener != null) {
                                    // TODO need better error mapping
                                    final PoyntError error = new PoyntError(PoyntError.CHECK_CARD_FAILURE);
                                    error.setThrowable(t);
                                    try {
                                        listener.onResponse(transaction, requestId, error);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onLoginRequired() throws RemoteException {
                        final PoyntError error = new PoyntError(PoyntError.CHECK_CARD_FAILURE);
                        try {
                            listener.onResponse(null, requestId, error);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                        final PoyntError error = new PoyntError(PoyntError.CHECK_CARD_FAILURE);
                        try {
                            listener.onResponse(null, requestId, error);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void getTransaction(final String transactionId,
                               final String requestId,
                               final IPoyntTransactionServiceListener listener) throws RemoteException {
        Log.d(TAG, "getTransaction: " + transactionId);
        // get cached transaction
        final Transaction transaction = transactionCache.get(transactionId);
        // if not found in cache get it from Poynt
        //TODO: figure out if we can get it from Converge
        if (transaction == null) {
            Log.d(TAG, "Transaction not found in cache - calling PoyntTransactionService");
            mPoyntTransactionService.getTransaction(transactionId, requestId, new IPoyntTransactionServiceListener.Stub() {
                @Override
                public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {
                    // update in cache
                    transactionCache.put(transactionId, transaction);
                    listener.onResponse(transaction, requestId, null);
                }

                @Override
                public void onLoginRequired() throws RemoteException {
                    // failed to obtain transaction
                    final PoyntError error = new PoyntError(PoyntError.CODE_API_ERROR);
                    listener.onResponse(transaction, requestId, error);
                }

                @Override
                public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                    // failed to obtain transaction
                    final PoyntError error = new PoyntError(PoyntError.CHECK_CARD_FAILURE);
                    listener.onResponse(transaction, requestId, error);
                }
            });
        } else {
            Log.d(TAG, "Found transaction in cache");
            listener.onResponse(transaction, requestId, null);
        }
    }

    public void reverseTransaction(
            final String transactionId,
            final EMVData emvData,
            final String requestId) throws RemoteException {
        Log.d(TAG, "reverseTransaction: " + transactionId);

        // get transaction from poynt service first
        getTransaction(transactionId, requestId, new IPoyntTransactionServiceListener.Stub() {
            @Override
            public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {
                // update in converge
                final ElavonTransactionRequest request = convergeMapper.getTransactionReversalRequest(
                        transaction.getFundingSource(),
                        transaction.getProcessorResponse().getRetrievalRefNum());
                convergeService.update(request, new ConvergeCallback<ElavonTransactionResponse>() {
                    @Override
                    public void onResponse(final ElavonTransactionResponse elavonResponse) {
                        Log.d(TAG, elavonResponse != null ? elavonResponse.toString() : "n/a");
                        if (elavonResponse.isSuccess()) {
                            Log.i(TAG, "reverseTransaction: " + transaction.getId() + " SUCCESS");
                        } else {
                            Log.e(TAG, "reverseTransaction: " + transaction.getId() + " FAILED");
                        }
                    }

                    @Override
                    public void onFailure(final Throwable t) {
                        t.printStackTrace();
                        Log.e(TAG, "reverseTransaction: " + transaction.getId() + " FAILED");
                    }
                });
            }

            @Override
            public void onLoginRequired() throws RemoteException {
                Log.e(TAG, "Failed to get transaction:" + transactionId);
            }

            @Override
            public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                Log.e(TAG, "Failed to get transaction:" + transactionId);
            }
        });


    }

    public void refundTransaction(final Transaction transaction,
                                  Transaction parentTransaction,
                                  final String requestId,
                                  final IPoyntTransactionServiceListener listener) {
        Log.d(TAG, "refundTransaction");

        if (parentTransaction != null) {
            if (!Boolean.TRUE.equals(transaction.getFundingSource().isDebit())) {
                // set the funding source from parent transaction
                transaction.setFundingSource(parentTransaction.getFundingSource());
            }
            transaction.setProcessorResponse(parentTransaction.getProcessorResponse());
        }
        final ElavonTransactionRequest request = convergeMapper.getTransactionRequest(transaction);
        convergeService.create(request, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse elavonResponse) {
                try {
                    convergeMapper.mapTransactionResponse(elavonResponse, transaction);
                    listener.onResponse(transaction, requestId, null);
                } catch (final RemoteException e) {
                    Log.e(TAG, "Failed to respond", e);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                // TODO need better error mapping
                final PoyntError error = new PoyntError(PoyntError.CHECK_CARD_FAILURE);
                error.setThrowable(t);
                try {
                    listener.onResponse(transaction, "", error);
                } catch (final RemoteException e) {
                    Log.e(TAG, "Failed to respond", e);
                }
            }
        });
    }

    public void processBalanceInquiry(
            final BalanceInquiry balanceInquiry,
            final String requestId,
            final IPoyntTransactionBalanceInquiryListener listener) {
        Log.d(TAG, "processBalanceInquiry");

        final ElavonTransactionRequest request = convergeMapper.getBalanceInquiryRequest(balanceInquiry);
        convergeService.create(request, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse elavonResponse) {
                try {
                    convergeMapper.mapBalanceInquiryResponse(elavonResponse, balanceInquiry);
                    listener.onResponse(balanceInquiry, null);
                } catch (final RemoteException e) {
                    Log.e(TAG, "Failed to respond", e);
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                // TODO need better error mapping
                final PoyntError error = new PoyntError(PoyntError.CHECK_CARD_FAILURE);
                error.setThrowable(t);
                try {
                    listener.onResponse(balanceInquiry, error);
                } catch (final RemoteException e) {
                    Log.e(TAG, "Failed to respond", e);
                }
            }
        });

    }

    public void generateToken(final String cardNumber, final String expiry, final ConvergeCallback<ElavonTransactionResponse> callback) {
        Log.d(TAG, "generateToken");
        convergeService.generateToken(cardNumber, expiry, callback);
    }

    public void settle(final List<String> transactionIdList, final ConvergeCallback<ElavonSettleResponse> callback) {
        Log.d(TAG, "settle");
        convergeService.settle(transactionIdList, callback);
    }
}
