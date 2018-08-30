package com.elavon.converge.config;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.elavon.converge.ElavonConvergeProcessorApplication;

import co.poynt.api.model.Business;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntBusinessProcessorDataListener;
import co.poynt.os.services.v1.IPoyntBusinessReadListener;
import co.poynt.os.services.v1.IPoyntBusinessService;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 *
 */
public class LoadBusinessIntentService extends IntentService {
    private static final String TAG = LoadBusinessIntentService.class.getSimpleName();

    private IPoyntBusinessService mBusinessService;
    private IPoyntBusinessReadListener listener;

//    private boolean serviceBound;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "business services connected");
            mBusinessService = IPoyntBusinessService.Stub.asInterface(service);
            loadBusiness();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBusinessService = null;
            Log.d(TAG, "business services disconnected");
        }
    };

    public LoadBusinessIntentService() {
        super("LoadBusinessIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_BUSINESS_SERVICE), serviceConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mBusinessService != null){
            loadBusiness();
        }else{
            bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_BUSINESS_SERVICE), serviceConnection,
                    BIND_AUTO_CREATE);
        }
    }

    private void loadBusiness() {
        Log.d(TAG, "loading business data with processor from cloud");
        try {
            mBusinessService.getBusinessWithProcessorData(new IPoyntBusinessProcessorDataListener.Stub() {
                @Override
                public void onResponse(Business business, PoyntError poyntError) throws RemoteException {
                    if(poyntError != null) {
                        Log.d(TAG, "loading business data with processor from cloud failed");
                    } else {
                        Log.d(TAG, "business data with processor from cloud succeded");
                        ElavonConvergeProcessorApplication.getInstance().setBusiness(business);
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
