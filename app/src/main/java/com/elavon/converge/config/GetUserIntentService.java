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
            mSessionService.getCurrentUser(new IPoyntSessionServiceListener.Stub() {
                @Override
                public void onResponse(Account account, PoyntError poyntError) throws RemoteException {
                    loadUserInfoFromDB(account);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void loadUserInfoFromDB(Account account) {
        AccountManager accountManager = (AccountManager) getSystemService(Context.ACCOUNT_SERVICE);
        String selection = null;
        String[] selectionArgs = null;
        String accountName = account.name;

        if (accountName == null) {
            Log.d(TAG, "first name");
            return;
        }
        Log.d(TAG, "Account Acquired: " + accountName + " making db call");

        selection = BusinessContract.BusinessUserColumns.FIRST_NAME + " = ?";
        selectionArgs = new String[]{accountName};

        Cursor mCursor = getContentResolver().query(
                BusinessContract.BusinessUsers.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
        );
        String firstName = null;
        String lastName = null;
        String nickName = null;

        if (mCursor != null && mCursor.getCount() > 0) {
            int firstNameIndex = mCursor.getColumnIndex(BusinessContract.BusinessUserColumns.FIRST_NAME);
            int lastNameIndex = mCursor.getColumnIndex(BusinessContract.BusinessUserColumns.LAST_NAME);
            int nickNameIndex = mCursor.getColumnIndex(BusinessContract.BusinessUserColumns.NICK_NAME);

            while (mCursor.moveToNext()) {
                firstName = mCursor.getString(firstNameIndex);
                lastName = mCursor.getString(lastNameIndex);
                nickName = mCursor.getString(nickNameIndex);
                break;
            }
            mCursor.close();
        }
        Log.d(TAG, "Result from db: " + firstName + " " + lastName + " " + nickName);
        if (mCursor != null) {
            mCursor.close();
        }
        ElavonConvergeProcessorApplication.getInstance().setCurrentUserInfo(nickName, firstName, lastName);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}