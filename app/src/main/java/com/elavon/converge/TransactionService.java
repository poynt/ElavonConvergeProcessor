package com.elavon.converge;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.elavon.converge.inject.AppComponent;
import com.elavon.converge.inject.AppModule;
import com.elavon.converge.inject.DaggerAppComponent;
import com.elavon.converge.core.TransactionManager;

import java.util.Map;

import javax.inject.Inject;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.Transaction;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntCheckCardListener;
import co.poynt.os.services.v1.IPoyntTransactionBalanceInquiryListener;
import co.poynt.os.services.v1.IPoyntTransactionService;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class TransactionService extends Service {

    private static final String TAG = "TransactionService";
    /**
     * Replaces null listener
     */
    private static final IPoyntTransactionServiceListener EMPTY_LISTENER = new IPoyntTransactionServiceListener() {
        @Override
        public void onResponse(Transaction transaction, String requestId, PoyntError poyntError) throws RemoteException {
        }

        @Override
        public void onLoginRequired() throws RemoteException {
        }

        @Override
        public void onLaunchActivity(Intent intent, String s) throws RemoteException {
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    };
    private final ServiceConnection mTransactionServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mPoyntTransactionService = IPoyntTransactionService.Stub.asInterface(iBinder);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mPoyntTransactionService = null;
        }
    };

    @Inject
    protected TransactionManager transactionManager;
    private IPoyntTransactionService mPoyntTransactionService;

    @Override
    public void onCreate() {
        final AppComponent component = DaggerAppComponent.builder().appModule(new AppModule(this.getApplicationContext())).build();
        component.inject(this);
        bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_TRANSACTION_SERVICE), mTransactionServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        unbindService(mTransactionServiceConnection);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IPoyntTransactionService.Stub mBinder = new IPoyntTransactionService.Stub() {

        @Override
        public void createTransaction(Transaction transaction, String requestId, IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "createTransaction: " + requestId);
        }

        @Override
        public void processTransaction(final Transaction transaction, final String requestId, final IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "processTransaction: " + requestId);
            Log.d(TAG, "Transaction:" + transaction.toString());
            // if you want to collect a new pin after processing a card
            //transactionManager.collectNewPin(transaction, requestId, listener);

            // if you want to collect MSR
            // otherwise just process as usual
            transactionManager.processTransaction(transaction, requestId, listener);
        }

        @Override
        public void voidTransaction(String transactionId, EMVData emvData, String requestId, IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "voidTransaction: " + requestId);
            transactionManager.voidTransaction(transactionId, emvData, requestId, listener);
        }

        @Override
        public void captureTransaction(String transactionId,
                                       AdjustTransactionRequest transaction,
                                       String requestId,
                                       IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "captureTransaction: " + requestId);
            transactionManager.captureTransaction(transactionId, transaction, requestId, listener);

        }

        @Override
        public void captureAllTransactions(String requestId) throws RemoteException {
            Log.d(TAG, "captureAllTransactions w/ RequestId:" + requestId);

        }

        @Override
        public void updateTransaction(
                final String transactionId,
                final AdjustTransactionRequest adjustTransactionRequest,
                final String requestId,
                final IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "updateTransaction: " + requestId);
            final IPoyntTransactionServiceListener originalListener = listener == null ? EMPTY_LISTENER : listener;

            // get transaction from poynt service first
            mPoyntTransactionService.getTransaction(transactionId, requestId, new IPoyntTransactionServiceListener.Stub() {
                @Override
                public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {
                    // update in converge
                    transactionManager.updateTransaction(transaction, adjustTransactionRequest, requestId, new IPoyntTransactionServiceListener.Stub() {
                        @Override
                        public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {
                            // update in poynt service
                            mPoyntTransactionService.updateTransaction(transactionId, adjustTransactionRequest, requestId, null);
                            originalListener.onResponse(transaction, requestId, null);
                        }

                        @Override
                        public void onLoginRequired() throws RemoteException {
                            originalListener.onLoginRequired();
                        }

                        @Override
                        public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                            originalListener.onLaunchActivity(intent, s);
                        }
                    });
                }

                @Override
                public void onLoginRequired() throws RemoteException {
                    originalListener.onLoginRequired();
                }

                @Override
                public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                    originalListener.onLaunchActivity(intent, s);
                }
            });
        }

        @Override
        public void captureEmvData(String transactionId, EMVData emvData, String requestId) throws RemoteException {
            Log.d(TAG, "capture EMV Data: " + transactionId);
            if (emvData != null) {
                Map<String, String> tags = emvData.getEmvTags();
                for (Map.Entry entry : tags.entrySet()) {
                    Log.d(TAG, "Tag " + entry.getKey() + " : " + entry.getValue());
                }
            }
        }

        @Override
        public void getBalanceInquiry(BalanceInquiry balanceInquiry, String requestId, IPoyntTransactionBalanceInquiryListener iPoyntTransactionBalanceInquiryListener) throws RemoteException {
            Log.d(TAG, "getBalanceInquiry: " + requestId);
        }

        @Override
        public void reverseTransaction(String originalRequestId, String originalTransactionId, EMVData emvData, String requestId) throws RemoteException {
            Log.d(TAG, "reverseTransaction: " + originalRequestId);
            if (emvData != null) {
                Log.d(TAG, "emvData received");
                if (emvData.getEmvTags() != null) {
                    for (Map.Entry entry : emvData.getEmvTags().entrySet()) {
                        Log.d(TAG, "tag:" + entry.getKey() + " value:" + entry.getValue());
                    }
                }
            }
        }

        @Override
        public void getTransaction(String transactionId, String requestId, IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "getTransaction: " + transactionId);
            transactionManager.getTransaction(transactionId, requestId, listener);
        }

        @Override
        public void saveTransaction(Transaction transaction, String requestId) throws RemoteException {
            Log.d(TAG, "saveTransaction: " + transaction.toString());
        }

        @Override
        public void checkCard(Payment payment, String serviceCode, String cardHolderName,
                              String expirationDate, String last4, String binRange, String AID,
                              String applicationLabel, String panSequenceNumber,
                              String issuerCountryCode, String encryptedPAN,
                              String encryptedTrack2, int issuerCodeTableIndex, String applicationPreferredName,
                              String keyIdentifier, String appCurrencyCode, IPoyntCheckCardListener callback)
                throws RemoteException {
            Log.d(TAG, "checkCard service code(" + serviceCode + ") cardHolderName(" +
                    cardHolderName + ") expirationDate(" + expirationDate
                    + ") last4(" + last4 + ") binRange(" + binRange + ") AID(" + AID + ")"
                    + ") applicationLabel(" + applicationLabel + ") panSequenceNumber("
                    + panSequenceNumber + ") issuerCountryCode(" + issuerCountryCode + ")"
                    + ") encryptedPAN(" + encryptedPAN + ")"
                    + ") encryptedTrack2(" + encryptedTrack2 + ")"
                    + ") issuerCodeTableIndex(" + issuerCodeTableIndex + ")"
                    + ") applicationPreferredName(" + applicationPreferredName + ")"
                    + ") keyIdentifier(" + keyIdentifier + ")");

            //shouldn't be called - just continue
            callback.onContinue();
        }
    };
}
