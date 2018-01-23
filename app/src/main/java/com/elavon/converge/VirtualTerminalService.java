package com.elavon.converge;

import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.elavon.converge.core.TransactionManager;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.processor.ConvergeCallback;

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
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class VirtualTerminalService {

    private static final String TAG = "VirtualTerminal";
    private final TransactionManager transactionManager;
    private VirtualTerminalListener virtualTerminalListener;

    public interface VirtualTerminalListener {
        void onProcessed(String message);
    }

    @Inject
    public VirtualTerminalService(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @JavascriptInterface
    public void processTransaction(
            final String cardNumber,
            final String cardPresent,
            final String expiry,
            final String cvv,
            final String address,
            final String zip,
            final String amount) {
        Log.i(TAG, "processTransaction");

        transactionManager.generateToken(cardNumber, expiry, new ConvergeCallback<ElavonTransactionResponse>() {
            @Override
            public void onResponse(final ElavonTransactionResponse response) {
                if (response.getToken() == null) {
                    virtualTerminalListener.onProcessed("token load failed.");
                    return;
                }

                virtualTerminalListener.onProcessed("token loaded successfully.");

                final boolean isCardPresent = "true".equals(cardPresent);
                final Transaction transaction = getTransaction(response.getToken(), isCardPresent, expiry, cvv, address, zip, amount);
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
                                        virtualTerminalListener.onProcessed("transaction success");
                                    } else {
                                        virtualTerminalListener.onProcessed("transaction failed with " + poyntError.getReason());
                                    }
                                }

                                @Override
                                public void onLoginRequired() throws RemoteException {
                                    virtualTerminalListener.onProcessed("transaction failed.");
                                }

                                @Override
                                public void onLaunchActivity(final Intent intent, final String s) throws RemoteException {
                                    virtualTerminalListener.onProcessed("transaction failed.");
                                }
                            });
                } catch (final RemoteException e) {
                    virtualTerminalListener.onProcessed("transaction failed.");
                }
            }

            @Override
            public void onFailure(final Throwable t) {
                virtualTerminalListener.onProcessed("token load failed.");
            }
        });
    }

    private Transaction getTransaction(
            final String token,
            final boolean isCardPresent,
            final String expiry,
            final String cvv,
            final String address,
            final String zip,
            final String amount) {

        final Long amountLong = Long.parseLong(amount);
        final Integer month = Integer.parseInt(expiry.substring(0, 2));
        final Integer year = Integer.parseInt(expiry.substring(2, 4));

        final TransactionAmounts amounts = new TransactionAmounts();
        amounts.setCurrency("USD");
        amounts.setOrderAmount(amountLong);
        amounts.setTransactionAmount(amountLong);

        final Card card = new Card();
        card.setNumberHashed(token);
        card.setExpirationMonth(month);
        card.setExpirationYear(year);

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
