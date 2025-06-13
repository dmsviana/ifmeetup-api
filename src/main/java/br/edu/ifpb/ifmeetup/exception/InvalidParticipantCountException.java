package br.edu.ifpb.ifmeetup.exception;

public class InvalidParticipantCountException extends BusinessValidationException {

    public InvalidParticipantCountException(String message) {
        super(message);
    }

    public InvalidParticipantCountException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}