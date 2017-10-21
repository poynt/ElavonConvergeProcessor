package com.elavon.converge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.elavon.converge.fragments.CheckCardFragment;
import com.elavon.converge.fragments.ZipCodeFragment;

import co.poynt.api.model.Transaction;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PoyntError;

public class PaymentActivity extends Activity implements
        ZipCodeFragment.OnFragmentInteractionListener, CheckCardFragment.OnFragmentInteractionListener {

    private static final String TAG = "PaymentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get the intent that started this activity
        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
        } else {
            Log.e(TAG, "PaymentActivity launched with no intent!");
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

    }


    @Override
    public void onFragmentInteraction(Transaction transaction, PoyntError error) {
        // Create intent to deliver some kind of result data
        Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
        result.putExtra("transaction", transaction);
        result.putExtra("error", error);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if ("VERIFY_ZIP_CODE".equals(action)) {
            final Transaction transaction = intent.getParcelableExtra("transaction");
//            Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
//            setResult(Activity.RESULT_CANCELED, result);
//            finish();
//            return;
            if (transaction == null) {
                Log.e(TAG, "PaymentActivity launched with no payment object");
                Intent result = new Intent(Intents.ACTION_COLLECT_PAYMENT_RESULT);
                setResult(Activity.RESULT_CANCELED, result);
                finish();
            } else {
                ZipCodeFragment zipCodeFragment =
                        ZipCodeFragment.newInstance(transaction);
                // prevent the merchant from dismissing the payment fragment by taping
                // anywhere on the screen
                zipCodeFragment.setCancelable(false);
                Log.d(TAG, "loading zip code fragment");
                getFragmentManager().beginTransaction()
                        .add(R.id.container, zipCodeFragment)
                        .commit();
            }
        } else if ("CHECK_CARD".equals(action)) {
            final Payment payment = intent.getParcelableExtra("payment");
            final String cardHolderName = intent.getStringExtra("cardHolderName");
            final String last4 = intent.getStringExtra("last4");
            final String binRange = intent.getStringExtra("binRange");
            final String aid = intent.getStringExtra("aid");
            final String expiration = intent.getStringExtra("expiration");
            final String serviceCode = intent.getStringExtra("serviceCode");
            final String applicationLabel = intent.getStringExtra("applicationLabel");
            final String panSequenceNumber = intent.getStringExtra("panSequenceNumber");
            final String issuerCountryCode = intent.getStringExtra("issuerCountryCode");
            final String encryptedPAN = intent.getStringExtra("encryptedPAN");
            final String encryptedTrack2 = intent.getStringExtra("encryptedTrack2");
            final int issuerCodeTableIndex = intent.getIntExtra("issuerCodeTableIndex", -1);
            final String applicationPreferredName = intent.getStringExtra("applicationPreferredName");
            final String keyIdentifier = intent.getStringExtra("keyIdentifier");
            if (payment == null) {
                Log.e(TAG, "PaymentActivity launched with no payment object");
                Intent result = new Intent(Intents.ACTION_CHECK_CARD_RESULT);
                setResult(Activity.RESULT_CANCELED, result);
                finish();
            } else {
                CheckCardFragment checkCardFragment = CheckCardFragment.newInstance(
                        payment, serviceCode, cardHolderName, last4, binRange, expiration, aid,
                        applicationLabel, panSequenceNumber, issuerCountryCode, encryptedPAN,
                        encryptedTrack2, issuerCodeTableIndex, applicationPreferredName,
                        keyIdentifier
                );
                checkCardFragment.setCancelable(false);
                Log.d(TAG, "loading check card fragment");
                getFragmentManager().beginTransaction()
                        .add(R.id.container, checkCardFragment)
                        .commit();
            }
        }
    }

    @Override
    public void onContinue(Payment payment) {
        Intent result = new Intent(Intents.ACTION_CHECK_CARD_RESULT);
        result.putExtra("payment", payment);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onCancel() {
        Intent result = new Intent(Intents.ACTION_CHECK_CARD_RESULT);
        setResult(Activity.RESULT_CANCELED, result);
        finish();
    }
}
