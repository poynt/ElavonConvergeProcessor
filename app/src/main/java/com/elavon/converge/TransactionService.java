package com.elavon.converge;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.elavon.converge.core.TransactionManager;
import com.elavon.converge.inject.AppComponent;
import com.elavon.converge.inject.AppModule;
import com.elavon.converge.inject.DaggerAppComponent;

import javax.inject.Inject;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.CaptureAllRequest;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.Transaction;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntCheckCardListener;
import co.poynt.os.services.v1.IPoyntTransactionBalanceInquiryListener;
import co.poynt.os.services.v1.IPoyntTransactionCaptureAllListener;
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

    @Inject
    protected TransactionManager transactionManager;

    @Override
    public void onCreate() {
        final AppComponent component = DaggerAppComponent.builder().appModule(new AppModule(this.getApplicationContext())).build();
        component.inject(this);
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
            transactionManager.processTransaction(transaction, requestId, listener);
        }

        @Override
        public void voidTransaction(String transactionId, final EMVData emvData, String requestId, IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "voidTransaction: " + requestId);

            final IPoyntTransactionServiceListener originalListener = listener == null ? EMPTY_LISTENER : listener;

            transactionManager.voidTransaction(transactionId, emvData, requestId, new IPoyntTransactionServiceListener.Stub() {
                @Override
                public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {
                    // update in poynt service
                    //mPoyntTransactionService.updateTransaction(transactionId, adjustTransactionRequest, requestId, null);
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
        public void captureTransaction(final String transactionId,
                                       final AdjustTransactionRequest adjustTransactionRequest,
                                       String requestId,
                                       final IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "captureTransaction: " + requestId);
            final IPoyntTransactionServiceListener originalListener = listener == null ? EMPTY_LISTENER : listener;
            transactionManager.captureTransaction(transactionId,
                    adjustTransactionRequest,
                    requestId,
                    new IPoyntTransactionServiceListener.Stub() {
                        @Override
                        public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {
                            // update in poynt service
                            //mPoyntTransactionService.captureTransaction(transactionId, adjustTransactionRequest, requestId, null);
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

            transactionManager.updateTransaction(transactionId, adjustTransactionRequest, requestId, new IPoyntTransactionServiceListener.Stub() {
                @Override
                public void onResponse(final Transaction transaction, final String requestId, final PoyntError poyntError) throws RemoteException {
                    // update in poynt service
                    //mPoyntTransactionService.updateTransaction(transactionId, adjustTransactionRequest, requestId, null);
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
        public void captureEmvData(final String transactionId, final EMVData emvData, String requestId) throws RemoteException {
            Log.d(TAG, "capture EMV Data: " + transactionId);
            if (emvData != null) {
                transactionManager.captureEMVData(transactionId, emvData, requestId);
            }
        }

        @Override
        public void getBalanceInquiry(
                final BalanceInquiry balanceInquiry,
                final String requestId,
                final IPoyntTransactionBalanceInquiryListener listener) throws RemoteException {
            Log.d(TAG, "getBalanceInquiry: " + requestId);
            transactionManager.processBalanceInquiry(balanceInquiry, requestId, listener);
        }

        @Override
        public void captureAllTransactionsWithOptions(String requestId,
                                                      Bundle bundle,
                                                      CaptureAllRequest captureAllRequest,
                                                      IPoyntTransactionCaptureAllListener iPoyntTransactionCaptureAllListener)
                                                      throws RemoteException {

        }

        @Override
        public void reverseTransaction(final String originalRequestId,
                                       final String originalTransactionId,
                                       final EMVData emvData,
                                       final String requestId) throws RemoteException {
            Log.d(TAG, "reverseTransaction: " + originalRequestId);
            transactionManager.reverseTransaction(originalTransactionId, emvData, requestId);
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
