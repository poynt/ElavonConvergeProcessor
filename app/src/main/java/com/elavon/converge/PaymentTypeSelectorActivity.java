package com.elavon.converge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.elavon.converge.fragments.MerchantAppSelectionFragment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import co.poynt.api.model.TransactionReference;
import co.poynt.api.model.TransactionReferenceType;
import co.poynt.os.model.CardSession;
import co.poynt.os.model.EMVApplication;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;

public class PaymentTypeSelectorActivity extends AppCompatActivity
        implements MerchantAppSelectionFragment.OnAppSelectionListener {

    private static final int COLLECT_PAYMENT_REQUEST = 13132;
    private static final String PERMISSION_PREPAYMENT_CARD_SESSION_EVENT
            = "poynt.permission.PREPAYMENT_SESSION_EVENT";
    private static final String TAG = PaymentTypeSelectorActivity.class.getSimpleName();

    private TextView mDumpTextView;
    private ScrollView mScrollView;
    private LinearLayout paymentTypeLayout;
    private CardSession currentCardSession;
    private int selectedApplication = -1;
    Button cancelBtn;
    MerchantAppSelectionFragment merchantAppSelectionFragment;
    boolean cardSessionRegistered = false;
    PAYMENT_TYPE selectedPaymentType;
    PAYMENT_TYPE detectedPaymentType;

    enum PAYMENT_TYPE {
        CREDIT, DEBIT, VOUCHER;
    }

    private static Set<String> CREDIT_BIN_RANGES = new HashSet<>();
    private static Set<String> DEBIT_BIN_RANGES = new HashSet<>();
    private static Set<String> VOUCHER_BIN_RANGES = new HashSet<>();

    private static Set<String> CREDIT_AIDs = new HashSet<>();
    private static Set<String> DEBIT_AIDs = new HashSet<>();
    private static Set<String> VOUCHER_AIDs = new HashSet<>();

    // load some test bin ranges
    static {
        // bin ranges for testing
        CREDIT_BIN_RANGES.add("524825");
        DEBIT_BIN_RANGES.add("420593");
        VOUCHER_BIN_RANGES.add("603342");
        // AIDs for testing
        CREDIT_AIDs.add("A000000025010801");
        CREDIT_AIDs.add("A000000025010801");
        CREDIT_AIDs.add("A0000000031010");
        DEBIT_AIDs.add("A0000000043060");
        DEBIT_AIDs.add("A0000000980840");
        DEBIT_AIDs.add("A0000000042203");
        DEBIT_AIDs.add("A0000001524010");
        DEBIT_AIDs.add("A0000000032010");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_type_selector);
        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
        paymentTypeLayout = (LinearLayout) findViewById(R.id.paymentTypeLayout);

        Button chargeBtn = (Button) findViewById(R.id.chargeBtn);
        chargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchPoyntPayment(1000l, detectedPaymentType);
            }
        });

        Button creditBtn = (Button) findViewById(R.id.creditBtn);
        creditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPaymentType = PAYMENT_TYPE.CREDIT;
                collectAmount();
            }
        });

        Button debitBtn = (Button) findViewById(R.id.debitBtn);
        debitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPaymentType = PAYMENT_TYPE.DEBIT;
                collectAmount();
            }
        });

        Button voucherBtn = (Button) findViewById(R.id.voucherBtn);
        voucherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPaymentType = PAYMENT_TYPE.VOUCHER;
                collectAmount();
            }
        });

        Button clearButton = (Button) findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLog();
            }
        });

        cancelBtn = (Button) findViewById(R.id.cancelButton);
        // hide cancel button by default
        cancelBtn.setVisibility(View.GONE);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send cancel event
                resetCardSession();
            }
        });
    }

    private void resetCardSession() {
        logReceivedMessage("Sending ACTION_CANCEL_PREPAYMENT_CARD_SESSION");
        Intent intent = new Intent(Intents.ACTION_CANCEL_PREPAYMENT_CARD_SESSION);
        intent.putExtra(Intents.INTENT_EXTRA_CARD_SESSION, currentCardSession);
        sendBroadcast(intent);
        // hide the cancel button
        cancelBtn.setVisibility(View.GONE);
        // show the payment types already
        paymentTypeLayout.setVisibility(View.VISIBLE);
        selectedPaymentType = null;
        detectedPaymentType = null;
    }

    @Override
    protected void onResume() {
        super.onPostResume();
        registerForCardSession();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterFromCardSession();
    }

    private void registerForCardSession() {
        if (!cardSessionRegistered) {
            Log.d(TAG, "registering for CARD_SESSION");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intents.ACTION_CARD_SESSION);
            registerReceiver(mPrepaymentCardFoundReceiver, intentFilter,
                    PERMISSION_PREPAYMENT_CARD_SESSION_EVENT, null);
            cardSessionRegistered = true;
        } else {
            Log.d(TAG, "Already registered for card session - not registering again.");
        }
    }

    private void unregisterFromCardSession() {
        if (cardSessionRegistered) {
            Log.d(TAG, "un-registering for CARD_SESSION");
            unregisterReceiver(mPrepaymentCardFoundReceiver);
            cardSessionRegistered = false;
        } else {
            Log.d(TAG, "Not registered for card session - nothing to unregister from.");
        }
    }

    private BroadcastReceiver mPrepaymentCardFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intents.ACTION_CARD_SESSION.equals(intent.getAction())) {
                logReceivedMessage("Received broadcast event: " + intent.getAction());
                currentCardSession = intent.getParcelableExtra(Intents.INTENT_EXTRA_CARD_SESSION);
                logReceivedMessage("Received card session binRange(" + currentCardSession.getBinRange()
                        + ") sessionId(" + currentCardSession.getCardSessionId()
                        + ") sessionType(" + currentCardSession.getCardSessionType().name()
                        + ") appList(" + (currentCardSession.getApplicationList() != null
                        ? currentCardSession.getApplicationList().size() : 0)
                        + ")");
                // if it's EMV - ask for app selection
                if (currentCardSession.getCardSessionType() == CardSession.CardSessionType.EMV) {
                    for (EMVApplication emvApplication : currentCardSession.getApplicationList()) {
                        logReceivedMessage("Index: " + emvApplication.getIndex() +
                                " AID:" + emvApplication.getCardAID() +
                                " Label:" + emvApplication.getLabel() +
                                " PreferredName:" + emvApplication.getPreferredName() +
                                " DfName:" + emvApplication.getDfName());
                    }

                    // if it's a single application - just detect whether it's credit/debit/voucher
                    if (currentCardSession.getApplicationList().size() == 1) {
                        EMVApplication emvApplication = currentCardSession.getApplicationList().get(0);
                        String aid = emvApplication.getCardAID();
                        if (aid == null || aid.length() <= 0) {
                            aid = emvApplication.getDfName();
                        }
                        selectedApplication = emvApplication.getIndex();
                        if (CREDIT_AIDs.contains(aid)) {
                            detectedPaymentType = PAYMENT_TYPE.CREDIT;
                        } else if (DEBIT_AIDs.contains(aid)) {
                            detectedPaymentType = PAYMENT_TYPE.DEBIT;
                        } else if (VOUCHER_AIDs.contains(aid)) {
                            detectedPaymentType = PAYMENT_TYPE.VOUCHER;
                        }
                    } else {
                        // if the merchant has already selected a payment type see if we can resolve to one
                        boolean resolvedToSingleAID = false;
                        if (selectedPaymentType != null) {
                            for (EMVApplication emvApplication : currentCardSession.getApplicationList()) {
                                String aid = emvApplication.getCardAID();
                                if (aid == null || aid.length() <= 0) {
                                    aid = emvApplication.getDfName();
                                }
                                if (selectedPaymentType == PAYMENT_TYPE.CREDIT) {
                                    if (CREDIT_AIDs.contains(aid)) {
                                        // we found a credit aid so we are good.
                                        detectedPaymentType = PAYMENT_TYPE.CREDIT;
                                        selectedApplication = emvApplication.getIndex();
                                        resolvedToSingleAID = true;
                                        break;
                                    }
                                } else if (selectedPaymentType == PAYMENT_TYPE.DEBIT) {
                                    if (DEBIT_AIDs.contains(aid)) {
                                        // we found a credit aid so we are good.
                                        detectedPaymentType = PAYMENT_TYPE.DEBIT;
                                        selectedApplication = emvApplication.getIndex();
                                        resolvedToSingleAID = true;
                                        break;
                                    }
                                } else if (selectedPaymentType == PAYMENT_TYPE.VOUCHER) {
                                    if (VOUCHER_AIDs.contains(aid)) {
                                        // we found a credit aid so we are good.
                                        detectedPaymentType = PAYMENT_TYPE.VOUCHER;
                                        selectedApplication = emvApplication.getIndex();
                                        resolvedToSingleAID = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!resolvedToSingleAID) {
                            // launch the app selection fragment
                            merchantAppSelectionFragment =
                                    MerchantAppSelectionFragment.newInstance();
                            merchantAppSelectionFragment.setApplicationItemList(currentCardSession.getApplicationList());
                            merchantAppSelectionFragment.show(getSupportFragmentManager(), "app-selection");
                        }
                    }
                } else {
                    // detect if it's credit or debit or voucher
                    if (currentCardSession.getBinRange() != null) {
                        if (CREDIT_BIN_RANGES.contains(currentCardSession.getBinRange())) {
                            detectedPaymentType = PAYMENT_TYPE.CREDIT;
                        } else if (DEBIT_BIN_RANGES.contains(currentCardSession.getBinRange())) {
                            detectedPaymentType = PAYMENT_TYPE.DEBIT;
                        } else if (VOUCHER_BIN_RANGES.contains(currentCardSession.getBinRange())) {
                            detectedPaymentType = PAYMENT_TYPE.VOUCHER;
                        }
                    }
                }
                if (detectedPaymentType != null) {
                    logReceivedMessage("Detected Payment Type: " + detectedPaymentType.name());
                }
                // show the cancel button
                cancelBtn.setVisibility(View.VISIBLE);
                // hide the payment types
                paymentTypeLayout.setVisibility(View.GONE);
            }
        }
    };

    public void logReceivedMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDumpTextView.append("<< " + message + "\n\n");
                mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
            }
        });
    }

    private void clearLog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDumpTextView.setText("");
                mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());
            }
        });
    }

    private void checkAndLaunchPaymentFragment(final Long amount, PAYMENT_TYPE paymentType) {

        // check if selected payment type selected card session if present
        if (detectedPaymentType != null) {
            if (selectedPaymentType != null) {
                if (detectedPaymentType != selectedPaymentType) {
                    // ask for confirmation
                    logReceivedMessage("Detected and Selected payment types do not match");

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Selected Payment Type (" +
                            selectedPaymentType.name() + ") does not match presented card type (" +
                            detectedPaymentType.name() + ")!");

                    // Set up the buttons
                    builder.setPositiveButton("Switch to " + detectedPaymentType.name(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    launchPoyntPayment(amount, detectedPaymentType);
                                }
                            });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            resetCardSession();
                        }
                    });
                    builder.show();
                    // return here so we don't process a transaction until we have a confirmation
                    // from merchant
                    return;
                }
            }
        }

        launchPoyntPayment(amount, paymentType);
    }

    private void launchPoyntPayment(Long amount, PAYMENT_TYPE paymentType) {


        Locale locale = new Locale("pt", "BR");
        String currencyCode = NumberFormat.getCurrencyInstance(locale).getCurrency().getCurrencyCode();

        Payment payment = new Payment();
        List<TransactionReference> references = new ArrayList<>();
        TransactionReference transactionReference = new TransactionReference();
        transactionReference.setType(TransactionReferenceType.CUSTOM);
        transactionReference.setCustomType("ReferenceId");
        transactionReference.setId(UUID.randomUUID().toString());
        references.add(transactionReference);
        payment.setReferences(references);
        payment.setAmount(amount);
        payment.setCurrency(currencyCode);
        payment.setDisablePaymentOptions(true);
        payment.setMultiTender(true);
        if (currentCardSession != null) {
            payment.setCardSessionId(currentCardSession.getCardSessionId());
            if (selectedApplication != -1) {
                payment.setApplicationIndex(selectedApplication);
            }
        }
        if (paymentType != null) {
            switch (paymentType) {
                case CREDIT:
                    payment.setCreditOnly(true);
                    break;
                case DEBIT:
                    payment.setDebitOnly(true);
                    break;
                case VOUCHER:
                    payment.setVoucher(true);
                    break;
                default:
                    break;
            }
        }

        // start Payment activity for result
        try {
            Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
            collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
            startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REQUEST);
        } catch (ActivityNotFoundException ex) {
            Log.e("ConfigurationTest", "Poynt Payment Activity not found - did you install PoyntServices?", ex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // hide the cancel button
        cancelBtn.setVisibility(View.GONE);
        // show the payment types
        paymentTypeLayout.setVisibility(View.VISIBLE);
        // reset stored card session
        currentCardSession = null;
        detectedPaymentType = null;
        // set selected payment type as null
        selectedPaymentType = null;


        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Payment payment = data.getParcelableExtra(Intents.INTENT_EXTRAS_PAYMENT);
                Log.d("ConfigurationTest", "Received onPaymentAction from PaymentFragment w/ Status:" + payment.getStatus());
                logReceivedMessage(payment.toString());
//                logReceivedMessage(payment.getStatus().name());
//                logReceivedMessage("# of transactions: " + payment.getTransactions().size());
//                for (Transaction transaction : payment.getTransactions()) {
//                    logReceivedMessage(transaction.toString());
//                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            logReceivedMessage("Payment Canceled");
        }
    }

    @Override
    public void onApplicationSelected(int index) {
        logReceivedMessage("Selected Application index:" + index);
        selectedApplication = index;
        // detect payment type based on aid selected
        EMVApplication emvApplication = currentCardSession.getApplicationList().get(index);
        String aid = emvApplication.getCardAID();
        if (aid == null || aid.length() <= 0) {
            aid = emvApplication.getDfName();
        }
        if (CREDIT_AIDs.contains(aid)) {
            detectedPaymentType = PAYMENT_TYPE.CREDIT;
        } else if (DEBIT_AIDs.contains(aid)) {
            detectedPaymentType = PAYMENT_TYPE.DEBIT;
        } else if (VOUCHER_AIDs.contains(aid)) {
            detectedPaymentType = PAYMENT_TYPE.VOUCHER;
        }
        if (detectedPaymentType != null) {
            logReceivedMessage("Detected Payment Type: " + detectedPaymentType.name());
        }
        if (merchantAppSelectionFragment != null) {
            merchantAppSelectionFragment.dismiss();
        }
    }

    @Override
    public void onCancelSelection() {
        logReceivedMessage("App selection canceled");
    }


    private void collectAmount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter amount to charge for " + selectedPaymentType.name());

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String amountStr = input.getText().toString();
                BigDecimal amount = new BigDecimal(amountStr);
                amount = amount.multiply(BigDecimal.valueOf(100));
                checkAndLaunchPaymentFragment(amount.longValue(), selectedPaymentType);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // re-enable payment options
                paymentTypeLayout.setVisibility(View.VISIBLE);
            }
        });
        builder.show();
    }
}
