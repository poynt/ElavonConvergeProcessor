package com.elavon.converge.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BusinessUpdatedReceiver extends BroadcastReceiver {
    private static final String TAG = BusinessUpdatedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());
        Intent serviceIntent = new Intent(context, LoadBusinessIntentService.class);
        context.startService(serviceIntent);
    }
}
