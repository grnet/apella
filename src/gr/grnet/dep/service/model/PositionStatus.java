package gr.grnet.dep.service.model;


public enum PositionStatus {
	ANAMONI_EGKRISIS(10),
	APORRIPTEA(20),
	EGKEKRIMENI(30),
	ENTAGMENI(40),
	ANOIXTI(50),
	KLEISTI(60),
	STELEXOMENI(70)
	;

	/** 
	 * Internal storage of priority field value, please see the Enum spec for clarification.
	 */ 
	private final int value;

	/** 
	 * Enum constructor.
	 * 
	 * @param value Value of priority.
	 */
	PositionStatus(final int value){
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
