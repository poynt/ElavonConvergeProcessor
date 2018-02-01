package com.elavon.converge.activities;

import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.elavon.converge.core.TransactionManager;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.processor.ConvergeCallback;
import com.elavon.converge.util.CardUtil;

import java.util.UUID;

import javax.inject.Inject;

import co.poynt.api.model.Address;
import co.poynt.api.model.Card;
import co.poynt.api.model.ClientContext;
import co.poynt.api.model.CustomerPresenceStatus;
import co.poynt.api.model.EntryMode;
import co.poynt.api.model.FundingSource;
import co.poynt.api.model.FundingSourceEntryDetails;
import co.poynt.api.model.FundingSourceType;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAction;
import co.poynt.api.model.TransactionAmounts;
import co.poynt.api.model.VerificationData;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class VirtualTerminalService {

    private static final String TAG = "VirtualTerminal";
    private final TransactionManager transactionManager;
    private VirtualTerminalListener virtualTerminalListener;
    private Payment payment;

    public interface VirtualTerminalListener {
        void onProcessed(Transaction transaction, String message);

        void onCancel();
    }

    @Inject
    public VirtualTerminalService(final TransactionManager transactionManager, final Payment payment) {
        this.transactionManager = transactionManager;
        this.payment = payment;
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
            final String expiry,
            final String cvv,
            final String address,
            final String zip) {
        Log.i(TAG, "processTransaction");

        transactionManager.generateToken(cardNumber, expiry, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse response) {
                if (response.getToken() == null) {
                    Log.e(TAG, "Failed to tokenize the card");
                    virtualTerminalListener.onProcessed(null, "Failed to tokenize the card, please try again!");
                    return;
                }

                Log.d(TAG, "Card tokenization successful...");

                final boolean isCardPresent = "true".equals(cardPresent);
                final Transaction transaction = getTransaction(cardNumber,
                        response.getToken(), isCardPresent, expiry, cvv, address, zip, payment);
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

    private Transaction getTransaction(
            final String cardNumber,
            final String token,
            final boolean isCardPresent,
            final String expiry,
            final String cvv,
            final String address,
            final String zip,
            final Payment payment) {

        final Integer month = Integer.parseInt(expiry.substring(0, 2));
        final Integer year = Integer.parseInt(expiry.substring(2, 4));

        final TransactionAmounts amounts = new TransactionAmounts();
        amounts.setCurrency("USD");
        amounts.setOrderAmount(payment.getAmount());
        amounts.setTransactionAmount(payment.getAmount());
        amounts.setTipAmount(payment.getTipAmount());

        final Card card = new Card();
        card.setNumberHashed(token);
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
        verificationData.setCvData(cvv);

        final FundingSource fundingSource = new FundingSource();
        fundingSource.setCard(card);
        fundingSource.setType(FundingSourceType.CREDIT_DEBIT);
        fundingSource.setEntryDetails(fundingSourceEntryDetails);
        fundingSource.setVerificationData(verificationData);

        final ClientContext clientContext = new ClientContext();
        // TODO get employee id
        clientContext.setEmployeeUserId(0L);

        final Transaction transaction = new Transaction();
        transaction.setAction(TransactionAction.SALE);
        transaction.setAmounts(amounts);
        transaction.setFundingSource(fundingSource);
        transaction.setContext(clientContext);
        return transaction;
    }

    void setVirtualTerminalListener(final VirtualTerminalListener virtualTerminalListener) {
        this.virtualTerminalListener = virtualTerminalListener;
    }
}
