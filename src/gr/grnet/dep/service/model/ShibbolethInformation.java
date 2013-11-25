package gr.grnet.dep.service.model;

import java.util.logging.Logger;

import javax.persistence.Embeddable;

@Embeddable
public class ShibbolethInformation {

	private static Logger logger = Logger.getLogger(ShibbolethInformation.class.getName());

	private String remoteUser;

	// givenName
	private String givenName;

	//sn
	private String sn;

	//schac-home-organization
	private String schacHomeOrganization;

	//unscoped_affiliation || primary_affiliation || affiliation
	private String affiliation;

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getSchacHomeOrganization() {
		return schacHomeOrganization;
	}

	public void setSchacHomeOrganization(String schacHomeOrganization) {
		this.schacHomeOrganization = schacHomeOrganization;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	////////////////

	public boolean isMissingRequiredFields() {
		return this.affiliation == null ||
			this.remoteUser == null ||
			this.givenName == null ||
			this.sn == null ||
			this.schacHomeOrganization == null;
	}

	@Override
	public String toString() {
		return "EduPersonAffiliation=" + getAffiliation() +
			" REMOTE_USER=" + getRemoteUser() +
			" GivenName=" + getGivenName() +
			" SchacHomeOrganization=" + getSchacHomeOrganization() +
			" Sn=" + getSn();
	}
}
