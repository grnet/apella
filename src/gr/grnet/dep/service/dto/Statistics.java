package gr.grnet.dep.service.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Statistics {

	private Date date = new Date();

	private Map<String, Map<String, Long>> users = new HashMap<String, Map<String, Long>>();

	private Map<String, Map<String, Long>> professors = new HashMap<String, Map<String, Long>>();

	private Long institutionRegulatoryFrameworks;

	private Long registers;

	private Map<String, Long> positions = new HashMap<String, Long>();

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Map<String, Map<String, Long>> getUsers() {
		return users;
	}

	public void setUsers(Map<String, Map<String, Long>> users) {
		this.users = users;
	}

	public Map<String, Map<String, Long>> getProfessors() {
		return professors;
	}

	public void setProfessors(Map<String, Map<String, Long>> professors) {
		this.professors = professors;
	}

	public Long getInstitutionRegulatoryFrameworks() {
		return institutionRegulatoryFrameworks;
	}

	public void setInstitutionRegulatoryFrameworks(Long institutionRegulatoryFrameworks) {
		this.institutionRegulatoryFrameworks = institutionRegulatoryFrameworks;
	}

	public Long getRegisters() {
		return registers;
	}

	public void setRegisters(Long registers) {
		this.registers = registers;
	}

	public Map<String, Long> getPositions() {
		return positions;
	}

	public void setPositions(Map<String, Long> positions) {
		this.positions = positions;
	}

}
