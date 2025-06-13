package br.edu.ifpb.ifmeetup.exception;


public class EventFullException extends BusinessValidationException {

    public EventFullException(String message) {
        super(message);
    }

    public EventFullException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}