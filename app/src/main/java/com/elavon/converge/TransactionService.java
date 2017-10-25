package com.elavon.converge;


import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.elavon.converge.core.TransactionManager;

import java.util.Map;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.BalanceInquiry;
import co.poynt.api.model.EMVData;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSourceType;
import co.poynt.api.model.Transaction;
import co.poynt.os.model.Payment;
import co.poynt.os.services.v1.IPoyntCheckCardListener;
import co.poynt.os.services.v1.IPoyntTransactionBalanceInquiryListener;
import co.poynt.os.services.v1.IPoyntTransactionService;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class TransactionService extends Service {

    private static final String TAG = "SampleProcessor";

    public TransactionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IPoyntTransactionService.Stub mBinder = new IPoyntTransactionService.Stub() {

        @Override
        public void createTransaction(Transaction transaction, String requestId, IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "createTransaction :" + requestId);
        }

        @Override
        public void processTransaction(final Transaction transaction, final String requestId, final IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "processTransaction :" + requestId);
            Log.d(TAG, "Transaction:" + transaction.toString());

            final TransactionManager transactionManager = ElavonConvergeProcessorApplication.getInstance().getTransactionManager();
            //transactionManager.processTransaction(transaction, requestId, listener);

            // if you want to collect a new pin after processing a card
            //transactionManager.collectNewPin(transaction, requestId, listener);

            // if you want to collect MSR
            if (transaction.getFundingSource().getType() == FundingSourceType.CREDIT_DEBIT
                    && transaction.getFundingSource().getEntryDetails().getEntryMode() == EntryMode.TRACK_DATA_FROM_MAGSTRIPE
                    ) {
                transactionManager.collectCVV(transaction, requestId, listener);
            } else {
                // otherwise just process as usual
                transactionManager.processTransaction(transaction, requestId, listener);
            }

        }

        @Override
        public void voidTransaction(String transactionId, EMVData emvData, String requestId, IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "voidTransaction :" + requestId);
            TransactionManager transactionManager = ElavonConvergeProcessorApplication.getInstance().getTransactionManager();
            transactionManager.voidTransaction(transactionId, emvData, requestId, listener);
        }

        @Override
        public void captureTransaction(String transactionId,
                                       AdjustTransactionRequest transaction,
                                       String requestId,
                                       IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "captureTransaction:" + requestId);
            TransactionManager transactionManager = ElavonConvergeProcessorApplication.getInstance().getTransactionManager();
            transactionManager.captureTransaction(transactionId,
                    transaction, requestId, listener);

        }

        @Override
        public void captureAllTransactions(String requestId) throws RemoteException {
            Log.d(TAG, "captureAllTransactions w/ RequestId:" + requestId);

        }

        @Override
        public void updateTransaction(String transactionId, AdjustTransactionRequest transaction, String requestId, IPoyntTransactionServiceListener listener) throws RemoteException {
            Log.d(TAG, "updateTransaction:" + requestId);
            TransactionManager transactionManager = ElavonConvergeProcessorApplication.getInstance().getTransactionManager();
            transactionManager.updateTransaction(transactionId, transaction, requestId, listener);
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
            Log.d(TAG, "getBalanceInquiry:" + requestId);
        }

        @Override
        public void reverseTransaction(String originalRequestId, String originalTransactionId, EMVData emvData, String requestId) throws RemoteException {
            Log.d(TAG, "reverseTransaction:" + originalRequestId);
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
            Log.d(TAG, "getTransaction:" + transactionId);
            TransactionManager transactionManager = ElavonConvergeProcessorApplication.getInstance().getTransactionManager();
            transactionManager.getTransaction(transactionId, requestId, listener);
        }

        @Override
        public void saveTransaction(Transaction transaction, String requestId) throws RemoteException {
            Log.d(TAG, "saveTransaction:" + transaction.toString());
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
            Intent paymentActivity = new Intent("CHECK_CARD");
            paymentActivity.setComponent(new ComponentName(getPackageName(), PaymentActivity.class.getName()));
            paymentActivity.putExtra("payment", payment);
            paymentActivity.putExtra("serviceCode", serviceCode);
            paymentActivity.putExtra("cardHolderName", cardHolderName);
            paymentActivity.putExtra("expiration", expirationDate);
            paymentActivity.putExtra("last4", last4);
            paymentActivity.putExtra("binRange", binRange);
            paymentActivity.putExtra("aid", AID);
            paymentActivity.putExtra("applicationLabel", applicationLabel);
            paymentActivity.putExtra("panSequenceNumber", panSequenceNumber);
            paymentActivity.putExtra("issuerCountryCode", issuerCountryCode);
            paymentActivity.putExtra("encryptedPAN", encryptedPAN);
//            paymentActivity.putExtra("encryptedTrack2", encryptedTrack2);
//            paymentActivity.putExtra("issuerCodeTableIndex", issuerCodeTableIndex);
//            paymentActivity.putExtra("applicationPreferredName", applicationPreferredName);
//            paymentActivity.putExtra("keyIdentifier", keyIdentifier);

            callback.onLaunchActivity(paymentActivity);
        }

    };
}
