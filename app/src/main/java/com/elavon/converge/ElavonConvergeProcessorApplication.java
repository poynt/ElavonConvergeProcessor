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
import com.elavon.converge.inject.AppComponent;
import com.elavon.converge.inject.AppModule;
import com.elavon.converge.inject.DaggerAppComponent;
import com.elavon.converge.processor.ConvergeClient;

import javax.inject.Inject;

import co.poynt.api.model.Business;
import co.poynt.api.model.Store;
import co.poynt.api.model.StoreDevice;
import co.poynt.os.model.PaymentSettings;

import static co.poynt.os.Constants.Accounts.POYNT_ACCOUNT_TYPE;
import static com.elavon.converge.Constants.DATASYNC_POLL_FREQUENCY;
import static com.elavon.converge.Constants.DATASYNC_PROVIDER_AUTHORITY;

public class ElavonConvergeProcessorApplication extends Application {
    private static final String TAG = ElavonConvergeProcessorApplication.class.getSimpleName();

    public static ElavonConvergeProcessorApplication instance;
    private Business business;
    private Business processorDataForBusiness;
    private PaymentSettings paymentSettings;

    @Inject
    protected ConvergeClient convergeClient;

    public static ElavonConvergeProcessorApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        instance = this;
        final AppComponent component = DaggerAppComponent.builder().appModule(new AppModule(this.getApplicationContext())).build();
        component.inject(this);
        Log.d(TAG, "loading business with Processor data service");
        startService(new Intent(this, LoadBusinessIntentService.class));
        schedulePeriodicSync();
    }

    public Business getBusiness() {
        return business;
    }

    public PaymentSettings getPaymentSettings() {
        return paymentSettings;
    }

    public Business getProcessorDataForBusiness() {
        return processorDataForBusiness;
    }

    public void setProcessorDataForBusiness(Business processorDataForBusiness) {
        this.processorDataForBusiness = processorDataForBusiness;
        Log.d(TAG, processorDataForBusiness.toString());
        if (processorDataForBusiness != null) {
            String userId = processorDataForBusiness.getProcessorData().get("com.elavon.converge:SSLUserId");
            String merchantId = null;
            String pin = null;
            Store store = processorDataForBusiness.getStores().get(0);
            if (store != null) {
                merchantId = store.getProcessorData().get("com.elavon.converge:SSLMerchantId");
                StoreDevice storeDevice = store.getStoreDevices().get(0);
                if (storeDevice != null) {
                    pin = storeDevice.getProcessorData().get("com.elavon.converge:SSLPin");
                }
            }
            if (pin != null && userId != null && merchantId != null) {
                Log.i(TAG, "Successfully loaded converge merchant credentials for userId:"
                        + userId);
                convergeClient.updateCredentials(merchantId, userId, pin);
            } else {
                Log.e(TAG, "Unable to load converge merchant credentials!");
                throw new RuntimeException("Converge Merchant Credentials MISSING! Please verify business settings.");
            }
        }
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
