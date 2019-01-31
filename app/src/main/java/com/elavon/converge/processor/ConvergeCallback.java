package com.elavon.converge.processor;

import com.elavon.converge.model.ElavonResponse;

public abstract class ConvergeCallback<T extends ElavonResponse> {

    public abstract void onResponse(T t);

    public abstract void onFailure(Throwable throwable);
}
