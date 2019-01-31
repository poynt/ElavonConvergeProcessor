package com.elavon.converge.sync;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.elavon.converge.inject.AppComponent;
import com.elavon.converge.inject.AppModule;
import com.elavon.converge.inject.DaggerAppComponent;
import com.elavon.converge.model.ElavonTransactionResponse;
import com.elavon.converge.model.ElavonTransactionSearchResponse;
import com.elavon.converge.processor.ConvergeCallback;
import com.elavon.converge.processor.ConvergeService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();
    ContentResolver mContentResolver;

    @Inject
    protected ConvergeService convergeService;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        final AppComponent component = DaggerAppComponent.builder().appModule(new AppModule(context)).build();
        component.inject(this);
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        final AppComponent component = DaggerAppComponent.builder().appModule(new AppModule(context)).build();
        component.inject(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        /*
         * Put the data transfer code here.
         */
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        final String startDate = dateFormat.format(currentTime);
        Log.d(TAG, "onPerformSync(): " + startDate);

        convergeService.fetchTransactions(startDate, null,
                new ConvergeCallback<ElavonTransactionSearchResponse>() {
                    @Override
                    public void onResponse(ElavonTransactionSearchResponse response) {
                        Log.d(TAG, "Received transactions since " + startDate);
                        if (response.getList() != null) {
                            Log.i(TAG, "fetchTransactions() returned " + response.getList().size() + " transactions.");
                            for (final ElavonTransactionResponse tr : response.getList()) {
                                Log.d(TAG, tr.toString());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e(TAG, "failed to fetch transactions from:" + startDate, throwable);
                    }
                });

    }
}
