package gr.grnet.dep.service.model.system;

import gr.grnet.dep.service.util.DEPConfigurationFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import java.io.File;

public final class WebConstants {

	public static Configuration conf;

	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	public static final String AUTHENTICATION_TOKEN_HEADER = "X-Auth-Token";

	public static final String ERROR_CODE_HEADER = "X-Error-Code";

	public static String FILES_PATH;

	// Static Initializations
	static {
		try {
			conf = DEPConfigurationFactory.getServerConfiguration();
			FILES_PATH = conf.getString("files.path");
			new File(FILES_PATH).mkdirs();
		} catch (ConfigurationException e) {
		}
	}

}
