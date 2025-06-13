package br.edu.ifpb.ifmeetup.exception;


public class DuplicateParticipantException extends BusinessValidationException {

    public DuplicateParticipantException(String message) {
        super(message);
    }

    public DuplicateParticipantException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}