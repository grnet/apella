package gr.grnet.dep.service.model;

public enum PositionStatus {
	ENTAGMENI(10),
	ANOIXTI(20),
	KLEISTI(30),
	STELEXOMENI(40);

	/**
	 * Internal storage of priority field value, please see the Enum spec for
	 * clarification.
	 */
	private final int value;

	/**
	 * Enum constructor.
	 * 
	 * @param value Value of priority.
	 */
	PositionStatus(final int value) {
		this.value = value;
	}

	/**
	 * Current string value stored in the enum.
	 * 
	 * @return string value.
	 */
	public final int getValue() {
		return this.value;
	}
}
