package com.elavon.converge;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.elavon.converge.config.GetUserIntentService;
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
    private String currentUserFirstName, currentUserLastName, currentUserNickName;
    private AppComponent mAppComponent;

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
        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this.getApplicationContext())).build();
        mAppComponent.inject(this);
        Log.d(TAG, "loading business with Processor data service");
        startService(new Intent(this, LoadBusinessIntentService.class));
        loadUser();

    }

    public void loadUser(){
        Log.d(TAG, "Loading device user");
        startService(new Intent(this, GetUserIntentService.class));
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
            String userId = processorDataForBusiness.getProcessorData().get(Constants.SSL_USER_ID);
            Log.d(TAG, "fetched user id is: " + userId);
            String merchantId = null;
            String pin = null;
            Store store = processorDataForBusiness.getStores().get(0);
            if (store != null) {
                merchantId = store.getProcessorData().get(Constants.SSL_MERCHANT_ID);
                Log.d(TAG, "fetched SSL merchant id is: " + merchantId);
                StoreDevice storeDevice = store.getStoreDevices().get(0);
                if (storeDevice != null) {
                    pin = storeDevice.getProcessorData().get(Constants.SSL_PIN);
                    Log.d(TAG, "fetch SSL pin from cloud is: " + pin);
                }
            }
            updateProcessorCredentialsInSharedPref(userId, merchantId, pin);
            updateProcessorCredentialsInConvergeClient(userId, merchantId, pin);
            schedulePeriodicSync();
        }
    }

    public boolean isMerchantCredsAvailableInPref() {
        boolean merchantCredsExists = false;
        Log.d(TAG, "Checking for Merchant credentials in Shared Preference.");
        try {
            SharedPreferences prefs = getSharedPreferences(Constants.MERCHANT_CREDS_PREFS, MODE_PRIVATE);
            String userId = prefs.getString(Constants.SSL_USER_ID, null);
            String merchantId = prefs.getString(Constants.SSL_MERCHANT_ID, null);
            String sslPin = prefs.getString(Constants.SSL_PIN, null);
            Log.d(TAG, "Merchant credentials from Shared Preference " + userId + " " + merchantId + " ");
            if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(merchantId)
                    && !TextUtils.isEmpty(sslPin)) {
                merchantCredsExists = true;
                updateProcessorCredentialsInConvergeClient(userId, merchantId, sslPin);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while reading merchant creds from shared pref");
            merchantCredsExists = false;
        }

        return merchantCredsExists;
    }

    private void updateProcessorCredentialsInSharedPref(String userId, String merchantId, String pin) {
        try {
            if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(merchantId) && !TextUtils.isEmpty(pin)) {
                SharedPreferences prefs = getSharedPreferences(Constants.MERCHANT_CREDS_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.SSL_USER_ID, userId);
                editor.putString(Constants.SSL_MERCHANT_ID, merchantId);
                editor.putString(Constants.SSL_PIN, pin);
                editor.commit();
                Log.d(TAG, "Updated Merchant credentials to Shared Preference.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while updating merchant creds to shared pref");
        }
    }

    public void updateProcessorCredentialsInConvergeClient(String userId, String merchantId, String pin) {
        if (pin != null && userId != null && merchantId != null) {
            Log.i(TAG, "Successfully loaded converge merchant credentials for userId:"
                    + userId);
            convergeClient.updateCredentials(merchantId, userId, pin);
            Log.d(TAG, convergeClient.toString());
        } else {
            Log.e(TAG, "Unable to load converge merchant credentials!");
            throw new RuntimeException("Converge Merchant Credentials MISSING! Please verify business settings.");
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

    public AppComponent getAppComponent() {
        return mAppComponent;
    }


    public void setCurrentUserInfo(String nickName, String firstName, String lastName){
        currentUserNickName = nickName;
        currentUserFirstName = firstName;
        currentUserLastName = lastName;
    }

    public String getCurrentUserFirstName() {
        return currentUserFirstName;
    }

    public String getCurrentUserLastName() {
        return currentUserLastName;
    }

    public String getCurrentUserNickName() {
        return currentUserNickName;
    }
}
