package gr.grnet.dep.service.exceptions;


public class NotFoundException extends Exception {
    public NotFoundException() {
    }

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Throwable e) {
        super(e);
    }

    public NotFoundException(String message, Throwable e) {
        super(message, e);
    }
}
