package com.elavon.converge.config;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import co.poynt.os.model.Intents;
import co.poynt.os.services.v1.IPoyntBusinessReadListener;
import co.poynt.os.services.v1.IPoyntSecurityService;

public class LoadMerchantCredsIntentService extends IntentService {
    private static final String TAG = LoadMerchantCredsIntentService.class.getSimpleName();

    private IPoyntSecurityService mPoyntSecurityService;
    private IPoyntBusinessReadListener listener;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPoyntSecurityService = IPoyntSecurityService.Stub.asInterface(service);
            loadMerchantCreds();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPoyntSecurityService = null;
            Log.d(TAG, "business services disconnected");
        }
    };

    public LoadMerchantCredsIntentService() {
        super("LoadMerchantCredsIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_SECURITY_SERVICE), serviceConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mPoyntSecurityService != null){
            loadMerchantCreds();
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

    private void loadMerchantCreds() {

    }
}
