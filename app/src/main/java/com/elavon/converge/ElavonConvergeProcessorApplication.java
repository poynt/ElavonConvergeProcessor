package com.elavon.converge;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.elavon.converge.config.LoadBusinessIntentService;

import co.poynt.api.model.Business;
import co.poynt.os.model.PaymentSettings;

import static co.poynt.os.Constants.Accounts.POYNT_ACCOUNT_TYPE;
import static com.elavon.converge.Constants.DATASYNC_POLL_FREQUENCY;
import static com.elavon.converge.Constants.DATASYNC_PROVIDER_AUTHORITY;

public class ElavonConvergeProcessorApplication extends Application {
    private static final String TAG = ElavonConvergeProcessorApplication.class.getSimpleName();

    public static ElavonConvergeProcessorApplication instance;
    private Business business;
    private PaymentSettings paymentSettings;

    public static ElavonConvergeProcessorApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        instance = this;
        startService(new Intent(this, LoadBusinessIntentService.class));
        schedulePeriodicSync();
    }

    public Business getBusiness() {
        return business;
    }

    public PaymentSettings getPaymentSettings() {
        return paymentSettings;
    }

    public void setBusiness(Business business) {
        this.business = business;
        paymentSettings = PaymentSettings.create(business);
        Log.d(TAG, "Set business and payment settings");
    }

    private void schedulePeriodicSync() {
        Log.d(TAG, "schedulePeriodicSync called.");
        AccountManager accountManager =
                (AccountManager) this.getSystemService(
                        Context.ACCOUNT_SERVICE);

        //Get any Poynt.co account
        Account[] poyntAccounts = accountManager.getAccountsByType(POYNT_ACCOUNT_TYPE);

        if (poyntAccounts.length > 0) {
            ContentResolver.setSyncAutomatically(poyntAccounts[0], DATASYNC_PROVIDER_AUTHORITY,
                    true);
            ContentResolver.addPeriodicSync(
                    poyntAccounts[0],
                    DATASYNC_PROVIDER_AUTHORITY,
                    Bundle.EMPTY,
                    DATASYNC_POLL_FREQUENCY);
            Log.d(TAG, "Successfully scheduled periodic sync.");
        }
    }
}
