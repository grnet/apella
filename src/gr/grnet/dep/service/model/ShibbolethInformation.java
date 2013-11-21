package gr.grnet.dep.service.model;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Embeddable;
import javax.servlet.http.HttpServletRequest;

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

	////////////////

	public static ShibbolethInformation readShibbolethFields(HttpServletRequest request) {
		ShibbolethInformation result = new ShibbolethInformation();

		result.setGivenName(readShibbolethField(request, "givenName"));
		result.setSn(readShibbolethField(request, "sn"));
		result.setSchacHomeOrganization(readShibbolethField(request, "schac-home-organization"));
		result.setRemoteUser(readShibbolethField(request, "REMOTE_USER"));

		String affiliation = null;
		affiliation = readShibbolethField(request, "unscoped-affiliation");
		if (affiliation == null) {
			affiliation = readShibbolethField(request, "primary-affiliation");
		}
		if (affiliation == null) {
			affiliation = readShibbolethField(request, "affiliation");
		}
		if (affiliation != null && affiliation.toLowerCase().contains("faculty")) {
			affiliation = "faculty";
		}
		result.setAffiliation(affiliation);

		return result;
	}

	private static String readShibbolethField(HttpServletRequest request, String headerName) {
		try {
			Object value = request.getHeader(headerName);
			String field = null;
			if (value != null && !value.toString().isEmpty()) {
				field = new String(value.toString().getBytes("ISO-8859-1"), "UTF-8");
			}
			return field;
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "decodeAttribute: ", e);
			return null;
		}
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
