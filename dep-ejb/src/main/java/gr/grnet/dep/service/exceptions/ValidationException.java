package gr.grnet.dep.service.exceptions;


import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
