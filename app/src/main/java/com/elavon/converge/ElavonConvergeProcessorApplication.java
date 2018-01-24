package com.elavon.converge;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.elavon.converge.config.LoadBusinessIntentService;

import co.poynt.api.model.Business;
import co.poynt.os.model.PaymentSettings;

public class ElavonConvergeProcessorApplication extends Application {
    private static final String TAG = ElavonConvergeProcessorApplication.class.getSimpleName();

    public static ElavonConvergeProcessorApplication instance;
    private Business business;
    private PaymentSettings paymentSettings;
    public static ElavonConvergeProcessorApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        instance = this;
        startService(new Intent(this, LoadBusinessIntentService.class));
    }

    public Business getBusiness(){
        return business;
    }

    public PaymentSettings getPaymentSettings(){
        return paymentSettings;
    }

    public void setBusiness(Business business){
        this.business = business;
        paymentSettings = PaymentSettings.create(business);
        Log.d(TAG, "Set business and payment settings");
    }

}
