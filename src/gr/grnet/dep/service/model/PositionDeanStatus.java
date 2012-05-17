package gr.grnet.dep.service.model;

public enum PositionDeanStatus {
	ANAMONI_ELEGXOU(10),
	AGONI_EKLOGI(20),
	EPANALIPSI(30),
	DIORISMOS(100);

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
	PositionDeanStatus(final int value) {
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
