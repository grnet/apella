package gr.grnet.dep.service.exceptions;

public class NotEnabledException extends Exception {
    public NotEnabledException() {
    }

    public NotEnabledException(String message) {
        super(message);
    }

    public NotEnabledException(Throwable e) {
        super(e);
    }

    public NotEnabledException(String message, Throwable e) {
        super(message, e);
    }
}

