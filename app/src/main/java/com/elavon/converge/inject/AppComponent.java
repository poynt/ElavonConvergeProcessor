package com.elavon.converge.inject;

import com.elavon.converge.TransactionService;
import com.elavon.converge.activities.MainActivity;
import com.elavon.converge.activities.ManualEntryActivity;
import com.elavon.converge.sync.SyncAdapter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainActivity mainActivity);

    void inject(TransactionService transactionService);

    void inject(ManualEntryActivity manualEntryActivity);

    void inject(SyncAdapter syncAdapter);
}
