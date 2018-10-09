package com.elavon.converge.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


import co.poynt.os.contentproviders.BusinessContract;
import co.poynt.os.model.Intents;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntSessionService;
import co.poynt.os.services.v1.IPoyntSessionServiceListener;

public class DeviceUserFetcher {
    private static final String TAG = DeviceUserFetcher.class.getSimpleName();
    private Context mContext;
    private IPoyntSessionService mSessionService;
    private String firstName, lastName, nickName;
    private OnUserFetchListener mListener;

    public DeviceUserFetcher(Context context) {
        mContext = context;

    }

    public void fetchUser(OnUserFetchListener listener) {
        Log.d(TAG, "Fetching Device user");
        mListener = listener;
        boolean binded = mContext.bindService(Intents.getComponentIntent(Intents.COMPONENT_POYNT_SESSION_SERVICE),
                mServiceConnection, Context.BIND_AUTO_CREATE);
        if(binded){
            Log.d(TAG, "service binded");
        }else{
            Log.d(TAG, "service not binded");
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mSessionService = IPoyntSessionService.Stub.asInterface(iBinder);
            try {
                mSessionService.getCurrentUser(serviceListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "");
        }
    };

    private IPoyntSessionServiceListener serviceListener = new IPoyntSessionServiceListener.Stub() {
        @Override
        public void onResponse(Account account, PoyntError poyntError) throws RemoteException {
            AccountManager accountManager = (AccountManager) mContext.getSystemService(Context.ACCOUNT_SERVICE);
            String selection = null;
            String[] selectionArgs = null;
            String firstNameAccount = accountManager.getUserData(account,
                    "user_first_name");
            String lastNameAccount = accountManager.getUserData(account,
                    "user_last_name");

            if (firstNameAccount == null || lastNameAccount == null) {
                Log.d(TAG, "first name or last name is null");
                return;
            }
            Log.d(TAG, "Account Acquired: " + firstNameAccount + " making db call");

            selection = BusinessContract.BusinessUserColumns.FIRST_NAME + " = ? AND " +
                    BusinessContract.BusinessUserColumns.LAST_NAME + " = ?";
            selectionArgs = new String[]{firstName, lastName};

            Cursor mCursor = mContext.getContentResolver().query(
                    BusinessContract.BusinessUsers.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
            );

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
            if (mListener != null) {
                mListener.onDeviceUserFetched(firstName, lastName, nickName);
                mListener = null;
            }
            Log.d(TAG, "Unbinding service");
            mContext.unbindService(mServiceConnection);

        }
    };

    public interface OnUserFetchListener {
        void onDeviceUserFetched(String firstName, String lastName, String nickName);
    }
}
