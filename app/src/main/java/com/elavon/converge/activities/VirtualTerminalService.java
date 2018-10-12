package com.elavon.converge.activities;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.elavon.converge.core.TransactionManager;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.processor.ConvergeCallback;
import com.elavon.converge.util.CardUtil;

import java.util.UUID;

import co.poynt.api.model.Address;
import co.poynt.api.model.CVSkipReason;
import co.poynt.api.model.Card;
import co.poynt.api.model.ClientContext;
import co.poynt.api.model.CustomerPresenceStatus;
import co.poynt.api.model.EBTDetails;
import co.poynt.api.model.EBTType;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSource;
import co.poynt.api.model.FundingSourceAccountType;
import co.poynt.api.model.FundingSourceEntryDetails;
import co.poynt.api.model.FundingSourceType;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAction;
import co.poynt.api.model.TransactionAmounts;
import co.poynt.api.model.TransactionStatus;
import co.poynt.api.model.VerificationData;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;
import co.poynt.os.util.StringUtil;

public class VirtualTerminalService {

    private static final String TAG = "VirtualTerminal";
    private final TransactionManager transactionManager;
    private VirtualTerminalListener virtualTerminalListener;
    private Payment payment;
    private String selectedPaymentMethodType;

    public interface VirtualTerminalListener {
        void onProcessed(Transaction transaction, String message);

        void onCancel();

        void showAlert(String message);

        void showProgressDialog();

        void stopProgressDialog();

        void collectAuthorizationCode(AuthorizationCodeCallback callback);

        void collectEBTVoucherNumber(AuthorizationCodeCallback callback);
    }

    public interface AuthorizationCodeCallback {
        void onApprovalCodeEntered(String approvalCode);

        void onApprovalCodeCanceled();
    }

    public VirtualTerminalService(final TransactionManager transactionManager,
                                  final Payment payment,
                                  final String selectedPaymentMethodType) {
        this.transactionManager = transactionManager;
        this.payment = payment;
        this.selectedPaymentMethodType = selectedPaymentMethodType;
    }

    @JavascriptInterface
    public void cancelTransaction() {
        Log.i(TAG, "cancelTransaction");
        virtualTerminalListener.onCancel();
    }

    @JavascriptInterface
    public void processTransaction(
            final String cardNumber,
            final String cardPresent,
            final String expiryMonth,
            final String expriryYear,
            final String cvv,
            final String zip,
            final String address) {
        Log.i(TAG, "processTransaction");

        if (StringUtil.isEmpty(cardNumber)) {
            Log.e(TAG, "No card data found");
            //virtualTerminalListener.onProcessed(null, "Invalid card, please try again!");
            virtualTerminalListener.showAlert("Invalid card, please enter a valid card!");
            return;
        }

        // collect approval code if it's a force
        if (selectedPaymentMethodType.equalsIgnoreCase("FORCED_MANUAL_ENTRY")) {
            virtualTerminalListener.collectAuthorizationCode(new AuthorizationCodeCallback() {
                @Override
                public void onApprovalCodeEntered(String approvalCode) {
                    tokenizeCard(cardNumber, cardPresent, expiryMonth,
                            expriryYear, cvv, zip, address, approvalCode, null);
                }

                @Override
                public void onApprovalCodeCanceled() {
                    tokenizeCard(cardNumber, cardPresent, expiryMonth,
                            expriryYear, cvv, zip, address, null, null);
                }
            });
        } else if (selectedPaymentMethodType.equalsIgnoreCase("EBT_VOUCHER")) {
            virtualTerminalListener.collectAuthorizationCode(new AuthorizationCodeCallback() {
                @Override
                public void onApprovalCodeEntered(final String approvalCode) {
                    virtualTerminalListener.collectEBTVoucherNumber(new AuthorizationCodeCallback() {
                        @Override
                        public void onApprovalCodeEntered(String voucherNumber) {
                            tokenizeCard(cardNumber, cardPresent, expiryMonth,
                                    expriryYear, cvv, zip, address, approvalCode, voucherNumber);
                        }

                        @Override
                        public void onApprovalCodeCanceled() {
                            tokenizeCard(cardNumber, cardPresent, expiryMonth,
                                    expriryYear, cvv, zip, address, null, null);
                        }
                    });
                }

                @Override
                public void onApprovalCodeCanceled() {
                    tokenizeCard(cardNumber, cardPresent, expiryMonth,
                            expriryYear, cvv, zip, address, null, null);
                }
            });
        } else {
            tokenizeCard(cardNumber, cardPresent, expiryMonth,
                    expriryYear, cvv, zip, address, null, null);
        }

    }

    public void tokenizeCard(
            final String cardNumber,
            final String cardPresent,
            final String expiryMonth,
            final String expriryYear,
            final String cvv,
            final String zip,
            final String address,
            final String approvalCode,
            final String voucherNumber) {
        Log.i(TAG, "tokenizeCard");

        // show progress bar
        virtualTerminalListener.showProgressDialog();

        // if request is for verification - amount = 0 - then no need to tokenize
        if (payment.getAmount() == 0l) {
            final boolean isCardPresent = "true".equals(cardPresent);
            final Transaction transaction = getTransaction(cardNumber, null, isCardPresent,
                    expiryMonth + expriryYear.substring(2),
                    cvv, address, zip, approvalCode, voucherNumber, payment);
            try {
                transactionManager.processTransaction(
                        transaction,
                        UUID.randomUUID().toString(),
                        new IPoyntTransactionServiceListener.Stub() {
                            @Override
                            public void onResponse(final Transaction parentTransaction,
                                                   final String requestId,
                                                   final PoyntError poyntError) throws RemoteException {
                                if (poyntError == null) {
                                    virtualTerminalListener.onProcessed(parentTransaction, null);

                                } else {
                                    virtualTerminalListener.onProcessed(parentTransaction,
                                            "transaction failed with " + poyntError.getReason());
                                }
                            }

                            @Override
                            public void onLoginRequired() throws RemoteException {
                                Log.e(TAG, "Received login required - unexpected!");
                                virtualTerminalListener.onProcessed(null, "transaction failed.");
                            }

                            @Override
                            public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                                //no-op
                                Log.e(TAG, "Received launch activity - unexpected!");
                                virtualTerminalListener.onProcessed(null, "transaction failed.");
                            }
                        });
            } catch (final RemoteException e) {
                Log.e(TAG, "Received remoteException - unexpected!");
                virtualTerminalListener.onProcessed(null, "transaction failed.");
            }
        } else {

            transactionManager.generateToken(
                    cardNumber,
                    expiryMonth + expriryYear.substring(2),
                    new ConvergeCallback<ElavonTransactionResponse>() {
                        @Override
                        public void onResponse(final ElavonTransactionResponse response) {
                            if (response.getToken() == null) {
                                Log.e(TAG, "Failed to tokenize the card");
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        virtualTerminalListener.showAlert("Invalid card, please try again!");
                                    }
                                });
                                return;
                            }

                            Log.d(TAG, "Card tokenization successful...");

                            final boolean isCardPresent = "true".equals(cardPresent);
                            final Transaction transaction = getTransaction(cardNumber,
                                    response.getToken(), isCardPresent,
                                    expiryMonth + expriryYear.substring(2),
                                    cvv, address, zip, approvalCode, voucherNumber, payment);
                            try {
                                transactionManager.processTransaction(
                                        transaction,
                                        UUID.randomUUID().toString(),
                                        new IPoyntTransactionServiceListener.Stub() {
                                            @Override
                                            public void onResponse(final Transaction parentTransaction,
                                                                   final String requestId,
                                                                   final PoyntError poyntError) throws RemoteException {
                                                if (poyntError == null) {
                                                    virtualTerminalListener.onProcessed(parentTransaction, null);

                                                } else {
                                                    virtualTerminalListener.onProcessed(parentTransaction,
                                                            "transaction failed with " + poyntError.getReason());
                                                }
                                            }

                                            @Override
                                            public void onLoginRequired() throws RemoteException {
                                                Log.e(TAG, "Received login required - unexpected!");
                                                virtualTerminalListener.onProcessed(null, "transaction failed.");
                                            }

                                            @Override
                                            public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                                                //no-op
                                                Log.e(TAG, "Received launch activity - unexpected!");
                                                virtualTerminalListener.onProcessed(null, "transaction failed.");
                                            }
                                        });
                            } catch (final RemoteException e) {
                                Log.e(TAG, "Received remoteException - unexpected!");
                                virtualTerminalListener.onProcessed(null, "transaction failed.");
                            }
                        }

                        @Override
                        public void onFailure(final Throwable t) {
                            Log.e(TAG, "Failed to tokenize the card");
                            virtualTerminalListener.onProcessed(null, "Failed to tokenize the card, please try again!");
                        }
                    });
        }
    }

    private Transaction getTransaction(
            final String cardNumber,
            final String token,
            final boolean isCardPresent,
            final String expiry,
            final String cvv,
            final String address,
            final String zip,
            final String approvalCode,
            final String voucherNumber,
            final Payment payment) {

        final Transaction transaction = new Transaction();
        if (payment.isNonReferencedCredit()) {
            transaction.setAction(TransactionAction.REFUND);
        } else {
            transaction.setAction(TransactionAction.SALE);
        }

        final Integer month = Integer.parseInt(expiry.substring(0, 2));
        final Integer year = Integer.parseInt(expiry.substring(2, 4));

        final TransactionAmounts amounts = new TransactionAmounts();
        amounts.setCurrency("USD");
        amounts.setOrderAmount(payment.getAmount());
        amounts.setTransactionAmount(payment.getAmount());
        amounts.setTipAmount(payment.getTipAmount());
        transaction.setAmounts(amounts);

        final Card card = new Card();
        if (token != null) {
            card.setNumberHashed(token);
        } else {
            card.setNumber(cardNumber);
        }
        card.setExpirationMonth(month);
        card.setExpirationYear(year);
        if (cardNumber.length() >= 6) {
            card.setNumberFirst6(cardNumber.substring(0, 6));
            card.setType(CardUtil.cardTypeByFirst6(card.getNumberFirst6()));
        }
        // imp to not give away last 4 when card number length is < 12 - becomes easy to guess
        if (cardNumber.length() >= 12) {
            card.setNumberLast4(cardNumber.substring(cardNumber.length() - 4, cardNumber.length()));
        }

        final FundingSourceEntryDetails fundingSourceEntryDetails = new FundingSourceEntryDetails();
        fundingSourceEntryDetails.setEntryMode(EntryMode.KEYED);
        if (isCardPresent) {
            fundingSourceEntryDetails.setCustomerPresenceStatus(CustomerPresenceStatus.PRESENT);
        }

        final Address fundingSourceAddress = new Address();
        fundingSourceAddress.setLine1(address);
        fundingSourceAddress.setPostalCode(zip);

        final VerificationData verificationData = new VerificationData();
        verificationData.setCardHolderBillingAddress(fundingSourceAddress);
        if (StringUtil.notEmpty(cvv) && !"undefined".equalsIgnoreCase(cvv)) {
            verificationData.setCvData(cvv);
        } else {
            if (!isCardPresent) {
                verificationData.setCvSkipReason(CVSkipReason.BYPASSED);
            } else {
                verificationData.setCvSkipReason(CVSkipReason.BYPASSED);
            }
        }

        final FundingSource fundingSource = new FundingSource();
        fundingSource.setCard(card);
        fundingSource.setType(FundingSourceType.CREDIT_DEBIT);
        fundingSource.setEntryDetails(fundingSourceEntryDetails);
        fundingSource.setVerificationData(verificationData);

        // set the type based on "selectedPaymentMethodType"
        if (StringUtil.notEmpty(selectedPaymentMethodType)) {
            if (selectedPaymentMethodType.equalsIgnoreCase("FORCED_MANUAL_ENTRY")) {
                // make sure we set the authorization code
                transaction.setApprovalCode(approvalCode);
                transaction.setStatus(TransactionStatus.CAPTURED);
            } else if (selectedPaymentMethodType.equalsIgnoreCase("EBT_CASH")) {
                fundingSource.setAccountType(FundingSourceAccountType.EBT);
                EBTDetails ebtDetails = new EBTDetails();
                ebtDetails.setType(EBTType.CASH_BENEFIT);
                fundingSource.setEbtDetails(ebtDetails);
            } else if (selectedPaymentMethodType.equalsIgnoreCase("EBT_FS")) {
                fundingSource.setAccountType(FundingSourceAccountType.EBT);
                EBTDetails ebtDetails = new EBTDetails();
                ebtDetails.setType(EBTType.FOOD_STAMP);
                fundingSource.setEbtDetails(ebtDetails);
            } else if (selectedPaymentMethodType.equalsIgnoreCase("EBT_VOUCHER")) {
                fundingSource.setAccountType(FundingSourceAccountType.EBT);
                EBTDetails ebtDetails = new EBTDetails();
                ebtDetails.setType(EBTType.FOOD_STAMP_ELECTRONIC_VOUCHER);
                ebtDetails.setElectronicVoucherApprovalCode(approvalCode);
                ebtDetails.setElectronicVoucherSerialNumber(voucherNumber);
                fundingSource.setEbtDetails(ebtDetails);
            }

        }
        transaction.setFundingSource(fundingSource);

        final ClientContext clientContext = new ClientContext();
        // TODO get employee id
        clientContext.setEmployeeUserId(0L);
        transaction.setContext(clientContext);


        return transaction;
    }

    void setVirtualTerminalListener(final VirtualTerminalListener virtualTerminalListener) {
        this.virtualTerminalListener = virtualTerminalListener;
    }
}
