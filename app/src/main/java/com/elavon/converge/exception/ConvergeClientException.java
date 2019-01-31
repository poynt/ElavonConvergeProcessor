package com.elavon.converge.exception;

public class ConvergeClientException extends RuntimeException {

    boolean networkError;

    public ConvergeClientException(String message) {
        super(message);
    }

    public ConvergeClientException(String message, boolean networkError) {
        super(message);
        this.networkError = networkError;
    }

    public ConvergeClientException(Throwable cause) {
        super(cause);
    }

    public ConvergeClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public boolean isNetworkError() {
        return networkError;
    }

    public void setNetworkError(boolean networkError) {
        this.networkError = networkError;
    }
}
