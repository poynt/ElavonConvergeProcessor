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
            loadProcessorDataForBusiness();
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
        Log.d(TAG, "Creating Business Intent Service for downloading business and processor data.");
        bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_BUSINESS_SERVICE), serviceConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mBusinessService != null){
            Log.d(TAG, "onHandleIntent- Business Service connected and is ready to load business and processor data.");
            loadBusiness();
            loadProcessorDataForBusiness();
        } else{
            bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_BUSINESS_SERVICE), serviceConnection,
                    BIND_AUTO_CREATE);
        }
    }

    private void loadBusiness() {
        try {
            mBusinessService.getBusiness(new IPoyntBusinessReadListener.Stub() {
                @Override
                public void onResponse(Business business, PoyntError poyntError) throws RemoteException {
                    if (business != null){
                        ElavonConvergeProcessorApplication.getInstance().setBusiness(business);
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void loadProcessorDataForBusiness() {
        Log.d(TAG, "downaloading processor data for business from cloud");
        try {
            mBusinessService.getBusinessProcessorData(new IPoyntBusinessProcessorDataListener.Stub() {
                @Override
                public void onResponse(Business business, PoyntError poyntError) throws RemoteException {
                    if (poyntError != null) {
                        Log.d(TAG, "downloading processor data with business from cloud failed"
                                + poyntError.getReason() != null ? " with reason " + poyntError.getReason() : "");
                    } else {
                        Log.d(TAG, "downaloading processor data with business from cloud succeded");
                        ElavonConvergeProcessorApplication.getInstance().setProcessorDataForBusiness(business);
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception while downloading Processor data with business.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
