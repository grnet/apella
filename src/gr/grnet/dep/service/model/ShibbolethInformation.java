package gr.grnet.dep.service.model;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Embeddable;
import javax.servlet.http.HttpServletRequest;

@Embeddable
public class ShibbolethInformation {

	private static Logger logger = Logger.getLogger(ShibbolethInformation.class.getName());

	private boolean enabled = false;

	private String eduPersonTargetedID;

	// givenName, HTTP_GIVENNAME
	private String givenName;

	//sn, HTTP_SN
	private String sn;

	//schacHomeOrganization
	private String schacHomeOrganization;

	//eduPersonAffiliation -> unscoped_affiliation, HTTP_UNSCOPED_AFFILIATION
	//eduPersonPrimaryAffiliation -> primary_affiliation, HTTP_PRIMARY_AFFILIATION
	//eduPersonScopedAffiliation -> affiliation, HTTP_AFFILIATION
	private String eduPersonAffiliation;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getEduPersonTargetedID() {
		return eduPersonTargetedID;
	}

	public void setEduPersonTargetedID(String eduPersonTargetedID) {
		this.eduPersonTargetedID = eduPersonTargetedID;
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

	public String getEduPersonAffiliation() {
		return eduPersonAffiliation;
	}

	public void setEduPersonAffiliation(String eduPersonAffiliation) {
		this.eduPersonAffiliation = eduPersonAffiliation;
	}

	////////////////

	public boolean isMissingRequiredFields() {
		return this.eduPersonAffiliation == null ||
			this.eduPersonTargetedID == null ||
			this.givenName == null ||
			this.sn == null ||
			this.schacHomeOrganization == null;
	}

	////////////////

	public static ShibbolethInformation readShibbolethFields(HttpServletRequest request) {
		ShibbolethInformation result = new ShibbolethInformation();

		result.setEduPersonTargetedID(readShibbolethField(request, "HTTP_PERSON_TARGETED_ID", "personTargetedID"));
		result.setGivenName(readShibbolethField(request, "HTTP_GIVENNAME", "givenName"));
		result.setSn(readShibbolethField(request, "HTTP_SN", "sn"));
		result.setSchacHomeOrganization(readShibbolethField(request, "HTTP_HOME_ORGANIZATION", "homeOrganization"));

		String eduPersonAffiliation = null;
		eduPersonAffiliation = readShibbolethField(request, "HTTP_UNSCOPED_AFFILIATION", "unscoped_affiliation");
		if (eduPersonAffiliation == null) {
			eduPersonAffiliation = readShibbolethField(request, "HTTP_PRIMARY_AFFILIATION", "primary_affiliation");
		}
		if (eduPersonAffiliation == null) {
			eduPersonAffiliation = readShibbolethField(request, "HTTP_AFFILIATION", "affiliation");
		}
		result.setEduPersonAffiliation(eduPersonAffiliation);

		return result;
	}

	private static String readShibbolethField(HttpServletRequest request, String attributeName, String headerName) {
		try {
			Object value = request.getAttribute(attributeName);
			String field = null;
			if (value == null || value.toString().isEmpty()) {
				value = request.getHeader(headerName);
			}
			if (value != null && !value.toString().isEmpty()) {
				field = new String(value.toString().getBytes("ISO-8859-1"), "UTF-8");
				if (field.indexOf(";") != -1) {
					field = field.substring(0, field.indexOf(";"));
				}
			}
			return field;
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "decodeAttribute: ", e);
			return null;
		}
	}

}
