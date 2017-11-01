package com.elavon.converge.exception;

public class ConvergeMapperException extends RuntimeException {

    public ConvergeMapperException(String message) {
        super(message);
    }

    public ConvergeMapperException(Throwable cause) {
        super(cause);
    }

    public ConvergeMapperException(String message, Throwable cause) {
        super(message, cause);
    }
}
