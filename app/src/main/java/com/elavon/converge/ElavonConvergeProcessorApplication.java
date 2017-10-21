package com.elavon.converge;

import android.app.Application;

import com.elavon.converge.core.TransactionManager;

/**
 * Created by palavilli on 1/25/16.
 */
public class ElavonConvergeProcessorApplication extends Application {
    public static ElavonConvergeProcessorApplication instance;

    public static ElavonConvergeProcessorApplication getInstance() {
        return instance;
    }

    TransactionManager transactionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        transactionManager = TransactionManager.getInstance(this);
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}
