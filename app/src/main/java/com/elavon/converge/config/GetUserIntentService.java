package com.elavon.converge.config;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.elavon.converge.ElavonConvergeProcessorApplication;

import co.poynt.os.contentproviders.BusinessContract;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntSessionService;
import co.poynt.os.services.v1.IPoyntSessionServiceInfoListener;
import co.poynt.os.services.v1.IPoyntSessionServiceListener;

public class GetUserIntentService extends IntentService {
    private static final String TAG = GetUserIntentService.class.getSimpleName();

    private IPoyntSessionService mSessionService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "session services connected");
            mSessionService = IPoyntSessionService.Stub.asInterface(service);
            getCurrentAccount();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSessionService = null;
            Log.d(TAG, "session services disconnected");
        }
    };

    public GetUserIntentService() {
        super("GetUserIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating Session Intent Service for getting current user info.");
        bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_SESSION_SERVICE), serviceConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mSessionService != null) {
            Log.d(TAG, "onHandleIntent- Session Service connected and is ready to fetch current user data.");
            getCurrentAccount();
        } else {
            bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_SESSION_SERVICE), serviceConnection,
                    BIND_AUTO_CREATE);
        }
    }

    private void getCurrentAccount() {
        try {
            mSessionService.getCurrentUserInfo(new IPoyntSessionServiceInfoListener.Stub() {
                @Override
                public void onResponse(String userName, String firstName, String lastName, PoyntError poyntError) throws RemoteException {
                    ElavonConvergeProcessorApplication.getInstance().setCurrentUserInfo(userName, firstName, lastName);
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