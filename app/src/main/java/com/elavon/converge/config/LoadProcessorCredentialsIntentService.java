package com.elavon.converge.config;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.elavon.converge.Constants;
import com.elavon.converge.ElavonConvergeProcessorApplication;
import com.elavon.converge.model.ProcessorCredentials;

import co.poynt.api.model.BusinessCredentials;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntProcessorCredentialsListener;
import co.poynt.os.services.v1.IPoyntSecurityService;

public class LoadProcessorCredentialsIntentService extends IntentService {
    private static final String TAG = LoadProcessorCredentialsIntentService.class.getSimpleName();

    private IPoyntSecurityService mPoyntSecurityService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPoyntSecurityService = IPoyntSecurityService.Stub.asInterface(service);
            Log.d(TAG, "processor credentials service connected");
            //Check if the merchant creds are stored in shared pref
            if(!isMerchantCredsAvailableInPref()) {
                //If not stored in shared pref, fetch it from Poynt Services
                getProcessorCredentials();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPoyntSecurityService = null;
            Log.d(TAG, "processor credential service disconnected");
        }
    };

    public LoadProcessorCredentialsIntentService() {
        super("LoadProcessorCredentialsIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "inside oncreate processor credentials service");
        bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_SECURITY_SERVICE), serviceConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent:");
        if (mPoyntSecurityService != null){
            //Check if the merchant creds are stored in shared pref
            if(!isMerchantCredsAvailableInPref()) {
                //If not stored in shared pref, fetch it from Poynt Services
                getProcessorCredentials();
            }
        }else{
            bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_SECURITY_SERVICE), serviceConnection,
                    BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private boolean isMerchantCredsAvailableInPref() {
        boolean merchantCredsExists = false;
        Log.d(TAG, "checking preocessor credentials in shared pref");

        try {
            SharedPreferences prefs = getSharedPreferences(Constants.MERCHANT_CREDS_PREFS, MODE_PRIVATE);
            String userId = prefs.getString(Constants.SSL_USER_ID, null);
            String merchantId = prefs.getString(Constants.SSL_MERCHANT_ID, null);
            String sslPin = prefs.getString(Constants.SSL_PIN, null);

            if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(merchantId)
                    && !TextUtils.isEmpty(sslPin)) {
                merchantCredsExists = true;
                ProcessorCredentials processorCredentials = new ProcessorCredentials();
                processorCredentials.setSSLMerchantId(merchantId);
                processorCredentials.setSSLPin(sslPin);
                processorCredentials.setSSLUserId(userId);
                ElavonConvergeProcessorApplication.getInstance().setProcessorCredentials(processorCredentials);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while reading processor credentials from shared pref");
            merchantCredsExists = false;
        }

        return merchantCredsExists;
    }

    private void getProcessorCredentials() {
        try {
            Log.d(TAG, "getting processor credentials from cloud");

            mPoyntSecurityService.getProcessorCredentials(new IPoyntProcessorCredentialsListener.Stub() {
                @Override
                public void onResponse(BusinessCredentials businessCredentials, PoyntError poyntError) throws RemoteException {
                    if(poyntError != null) {
                        Log.e(TAG, "processor credential fetching failed " + poyntError);
                        return;
                    }

                    if (businessCredentials != null) {
                        Log.d(TAG, "received processor credentials from cloud");
                        String userId = businessCredentials.getBusinessCredentials().get(Constants.ELAVON_CONVERGE_SSL_USER_ID);
                        String merchantId = businessCredentials.getBusinessCredentials().get(Constants.ELAVON_CONVERGE_SSL_MERCHANT_ID);
                        String sslPin = businessCredentials.getBusinessCredentials().get(Constants.ELAVON_CONVERGE_SSL_PIN);

                        ProcessorCredentials processorCredentials = new ProcessorCredentials();
                        SharedPreferences prefs = getSharedPreferences(Constants.MERCHANT_CREDS_PREFS,
                                MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        Log.d(TAG, "userId " + userId + " merchantId " + merchantId);
                        if(!TextUtils.isEmpty(userId)) {
                            processorCredentials.setSSLUserId(userId);
                            editor.putString(Constants.SSL_USER_ID, userId);
                        }
                        if(!TextUtils.isEmpty(merchantId)) {
                            processorCredentials.setSSLUserId(merchantId);
                            editor.putString(Constants.SSL_MERCHANT_ID, merchantId);
                        }
                        if(!TextUtils.isEmpty(sslPin)) {
                            processorCredentials.setSSLUserId(sslPin);
                            editor.putString(Constants.SSL_PIN, sslPin);
                        }
                        editor.commit();
                        ElavonConvergeProcessorApplication.getInstance().setProcessorCredentials(processorCredentials);
                    }
                }

            });
        } catch (RemoteException e) {
            Log.e(TAG, "Exception while fetching processor credentials from cloud " + e.getMessage());
        }
    }
}
