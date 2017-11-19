package com.elavon.converge.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.util.LruCache;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.mapper.ConvergeMapper;
import com.elavon.converge.processor.ConvergeCallback;
import com.elavon.converge.processor.ConvergeService;

import javax.inject.Inject;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAction;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntTransactionService;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class TransactionManager {

    private static final String TAG = "TransactionManager";
    private IPoyntTransactionService mPoyntTransactionService;

    protected Context context;
    protected ConvergeService convergeService;
    protected ConvergeMapper convergeMapper;
    int cacheSize = 100; // 100 transactions
    private LruCache<String, Transaction> transactionCache;
    private LruCache<String, EMVData> emvDataCache;

    @Inject
    public TransactionManager(final Context context,
                              final ConvergeService convergeService,
                              final ConvergeMapper convergeMapper) {
        this.context = context;
        this.convergeService = convergeService;
        this.convergeMapper = convergeMapper;

        transactionCache = new LruCache<>(cacheSize);
        emvDataCache = new LruCache<>(cacheSize);
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

    /**
     * process transaction
     *
     * @param transaction
     * @param requestId
     * @param listener
     */

    public void processTransaction(final Transaction transaction,
                                   final String requestId,
                                   final IPoyntTransactionServiceListener listener) throws RemoteException {
        Log.d(TAG, "processTransaction");


        // if the transaction action is REFUND and if it has a parentId - let's get it first
        if (transaction.getAction() == TransactionAction.REFUND) {
            // get parent transaction from poynt service first
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
                // update in converge
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

                        final ElavonTransactionRequest request = convergeMapper.getTransactionVoidRequest(
                                transaction.getFundingSource().getEntryDetails(),
                                transaction.getProcessorResponse().getRetrievalRefNum());
                        convergeService.update(request, new ConvergeCallback<ElavonTransactionResponse>() {
                            @Override
                            public void onResponse(final ElavonTransactionResponse elavonResponse) {
                                Log.d(TAG, elavonResponse != null ? elavonResponse.toString() : "n/a");
                                if (elavonResponse.isSuccess()) {
                                    Log.i(TAG, "voidTransaction: " + transaction.getId() + " SUCCESS");
                                    if (listener != null) {
                                        convergeMapper.mapTransactionResponse(elavonResponse, transaction);
                                        try {
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
                                            final PoyntError error = new PoyntError(PoyntError.CHECK_CARD_FAILURE);
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
                        transaction.getFundingSource().getEntryDetails(),
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

        // set the funding source from parent transaction
        transaction.setFundingSource(parentTransaction.getFundingSource());
        transaction.setProcessorResponse(parentTransaction.getProcessorResponse());

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
}
