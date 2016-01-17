package io.bifroest.commons.net.jsonserver;

public class RequiredRequestFailedException extends RuntimeException {

    public RequiredRequestFailedException() {
    }

    public RequiredRequestFailedException(String message) {
        super(message);
    }

    public RequiredRequestFailedException(Throwable cause) {
        super(cause);
    }

    public RequiredRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
