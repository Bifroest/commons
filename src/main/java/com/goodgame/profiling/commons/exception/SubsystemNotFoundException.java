package com.goodgame.profiling.commons.exception;

public class SubsystemNotFoundException extends Exception {

    public SubsystemNotFoundException() {
    }

    public SubsystemNotFoundException(String message) {
        super(message);
    }

    public SubsystemNotFoundException(Throwable cause) {
        super(cause);
    }

    public SubsystemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}


