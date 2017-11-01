package com.elavon.converge.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.elavon.converge.model.ElavonTransactionRequest;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.mapper.ConvergeMapper;
import com.elavon.converge.processor.ConvergeCallback;
import com.elavon.converge.processor.ConvergeService;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAmounts;
import co.poynt.api.model.TransactionStatus;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntSecurityService;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class TransactionManager {

    private static final String TAG = "TransactionManager";
    private IPoyntSecurityService poyntSecurityService;

    protected Context context;
    protected ConvergeService convergeService;
    protected ConvergeMapper convergeMapper;
    protected Map<UUID, Transaction> transactionCache;

    @Inject
    public TransactionManager(final Context context, final ConvergeService convergeService, final ConvergeMapper convergeMapper) {
        this.context = context;
        this.convergeService = convergeService;
        this.convergeMapper = convergeMapper;
        this.transactionCache = new HashMap<>();
        bind();
    }

    public synchronized void bind() {
        if (poyntSecurityService == null) {
            ComponentName COMPONENT_POYNT_SECURITY_SERVICE = new ComponentName("co.poynt.services", "co.poynt.services.PoyntSecurityService");
            context.bindService(Intents.getComponentIntent(COMPONENT_POYNT_SECURITY_SERVICE), mConnection, Context.BIND_AUTO_CREATE);
        } else {
            // already connected ?
        }
    }

    public boolean isConnected() {
        if (poyntSecurityService != null) {
            return true;
        } else {
            return false;
        }
    }

    public void shutdown() {
        context.unbindService(mConnection);
    }

    /**
     * Class for interacting with the BusinessService
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "IPoyntSecurityService is now connected");
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            poyntSecurityService = IPoyntSecurityService.Stub.asInterface(service);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "IPoyntSecurityService has unexpectedly disconnected - reconnecting");
            poyntSecurityService = null;
            bind();
        }
    };

    public void processTransaction(final Transaction transaction, final String requestId, final IPoyntTransactionServiceListener listener) {
        Log.d(TAG, "PROCESSED TRANSACTION");

        final ElavonTransactionRequest request = convergeMapper.createSale(transaction);
        System.out.println(new Gson().toJson(request));
        convergeService.create(request, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(ElavonTransactionResponse elavonResponse) {
                try {
                    convergeMapper.handleMSRSaleResponse(transaction, elavonResponse);
                    listener.onResponse(transaction, requestId, null);
                } catch (RemoteException e) {
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
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to respond", e);
                }
            }
        });
    }

    public void captureTransaction(String transactionId, AdjustTransactionRequest adjustTransactionRequest, String requestId, IPoyntTransactionServiceListener listener) {
        Log.d(TAG, "CAPTURED TRANSACTION: " + transactionId);
        // get cached transaction
        Transaction transaction = transactionCache.get(UUID.fromString(transactionId));
        if (transaction == null) {
            // if we did not find one just generate a new one
            transaction = new Transaction();
            transaction.setId(UUID.fromString(transactionId));
            transaction.setCreatedAt(Calendar.getInstance());
        }
        transaction.setAmounts(adjustTransactionRequest.getAmounts());
        transaction.setStatus(TransactionStatus.CAPTURED);
        transaction.setUpdatedAt(Calendar.getInstance());
        try {
            transactionCache.put(transaction.getId(), transaction);
            if (listener != null) {
                listener.onResponse(transaction, requestId, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateTransaction(String transactionId, AdjustTransactionRequest adjustTransactionRequest, String requestId, IPoyntTransactionServiceListener listener) {
        Log.d(TAG, "UPDATE TRANSACTION: " + transactionId);
        // get cached transaction
        Transaction transaction = transactionCache.get(UUID.fromString(transactionId));
        if (transaction == null) {
            // if we did not find one just generate a new one
            transaction = new Transaction();
            transaction.setId(UUID.fromString(transactionId));
            transaction.setCreatedAt(Calendar.getInstance());
        }
        transaction.setAmounts(adjustTransactionRequest.getAmounts());
        transaction.setUpdatedAt(Calendar.getInstance());
        try {
            transactionCache.put(transaction.getId(), transaction);
            if (listener != null) {
                listener.onResponse(transaction, requestId, null);
            } else {
                Log.d(TAG, "ignoring callback as it's null");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void voidTransaction(String transactionId,
                                EMVData emvData,
                                String requestId,
                                IPoyntTransactionServiceListener listener) {
        // get cached transaction
        Transaction transaction = transactionCache.get(UUID.fromString(transactionId));
        if (transaction == null) {
            // if we did not find one just generate a new one
            transaction = new Transaction();
            transaction.setId(UUID.fromString(transactionId));
            transaction.setCreatedAt(Calendar.getInstance());
            TransactionAmounts amounts = new TransactionAmounts();
            amounts.setOrderAmount(100l);
            amounts.setTransactionAmount(100l);
            transaction.setAmounts(amounts);
        }
        transaction.setStatus(TransactionStatus.VOIDED);
        transaction.setUpdatedAt(Calendar.getInstance());

        try {
            transactionCache.put(transaction.getId(), transaction);
            listener.onResponse(transaction, requestId, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void getTransaction(String transactionId, String requestId, IPoyntTransactionServiceListener listener) {
        Log.d(TAG, "CAPTURED TRANSACTION: " + transactionId);
        // get cached transaction
        Transaction transaction = transactionCache.get(UUID.fromString(transactionId));
        try {
            listener.onResponse(transaction, requestId, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
