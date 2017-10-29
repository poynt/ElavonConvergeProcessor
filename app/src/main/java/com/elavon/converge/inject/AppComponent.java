package com.elavon.converge.inject;

import com.elavon.converge.TransactionService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(TransactionService transactionService);
}
