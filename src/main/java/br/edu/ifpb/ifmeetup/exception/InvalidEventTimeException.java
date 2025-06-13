package br.edu.ifpb.ifmeetup.exception;

public class InvalidEventTimeException extends BusinessValidationException {

    public InvalidEventTimeException(String message) {
        super(message);
    }

    public InvalidEventTimeException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}