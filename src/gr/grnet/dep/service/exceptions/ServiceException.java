package gr.grnet.dep.service.exceptions;

public class ServiceException extends Exception {

	private static final long serialVersionUID = 5519873880766238330L;

	private String errorKey = "";

	public ServiceException() {
		super();
	}

	public ServiceException(String errorKey) {
		super();
		this.errorKey = errorKey;
	}

	public ServiceException(String errorKey, Throwable e) {
		super(e);
		this.errorKey = errorKey;
	}

	public ServiceException(String errorKey, String message) {
		super(message);
		this.errorKey = errorKey;
	}

	public ServiceException(String errorKey, String message, Throwable e) {
		super(message, e);
		this.errorKey = errorKey;
	}

	public String getErrorKey() {
		return errorKey;
	}

}
