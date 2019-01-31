package com.elavon.converge.exception;

public class AppInitException extends RuntimeException {

    public AppInitException(String message) {
        super(message);
    }

    public AppInitException(Throwable cause) {
        super(cause);
    }

    public AppInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
