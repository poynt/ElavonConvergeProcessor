package com.elavon.converge.exception;

public class ConvergeClientException extends RuntimeException {

    public ConvergeClientException(String message) {
        super(message);
    }

    public ConvergeClientException(Throwable cause) {
        super(cause);
    }

    public ConvergeClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
