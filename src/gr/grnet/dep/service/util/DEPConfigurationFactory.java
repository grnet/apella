package gr.grnet.dep.service.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;



/**
 * @author chstath
 *
 */
public class DEPConfigurationFactory {
	
	private static final Logger logger = Logger.getLogger(DEPConfigurationFactory.class.getName());
	
	/**
	 * The filename of the file containing system properties.
	 */
	private static String serverConfigFilename = "dep-server.properties";
	
	/**
	 * The (single) configuration object
	 */
	private static Configuration serverConfiguration = null;

	private static FileChangedReloadingStrategy strategy = null;

	/**
	 * Exists only to defeat instantiation
	 */
	private DEPConfigurationFactory() {
		// Exists only to defeat instantiation.
	}

	/* 
	 * Because multiple classloaders are commonly used in many situations -
	 * including servlet containers - you can wind up with multiple singleton instances
	 * no matter how carefully you've implemented your singleton classes.
	 * If you want to make sure the same classloader loads your singletons,
	 * you must specify the classloader yourself; for example: 
	 */

	/**
	 * @param classname
	 * @return Class
	 * @throws ClassNotFoundException
	 */
	private static Class<?> getClass(String classname) throws ClassNotFoundException {
	      ClassLoader classLoader = DEPConfigurationFactory.class.getClassLoader();
	      return (classLoader.loadClass(classname));
	}	

	/**
	 * It returns the server configuration object
	 * 
	 * @return Configuration
	 * @throws ConfigurationException 
	 * 
	 */
	public synchronized static Configuration getServerConfiguration() throws ConfigurationException {
		try {
			if (serverConfiguration == null) {
				PropertiesConfiguration config = (PropertiesConfiguration) getClass(PropertiesConfiguration.class.getCanonicalName()).newInstance();
				//PropertiesConfiguration config = (PropertiesConfiguration) Class.forName("org.apache.commons.configuration.PropertiesConfiguration") .newInstance();
				config.setBasePath("");
				config.setFileName(serverConfigFilename);
				config.setEncoding("ISO-8859-1"); 
				// Set automatic reloading
				config.setReloadingStrategy(new VfsFileChangedReloadingStrategy());
				config.load();
				config.setAutoSave(true);
				serverConfiguration = config;
			}
			return serverConfiguration;
		}
		catch (ClassNotFoundException e) {
			throw new ConfigurationException(e);
		} catch (InstantiationException e) {
			throw new ConfigurationException(e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException(e);
		}
	}
	


	/**
	 * @throws ConfigurationException 
	 * 
	 */
	public static void save() throws ConfigurationException {
		((PropertiesConfiguration) serverConfiguration).save();
	}

	/**
	 * Extends the FileChangedReloadingStrategy from Commons Configuration, adding
	 * support for files in JBoss MC VFS.
	 *
	 * @author past
	 */
	static class VfsFileChangedReloadingStrategy extends FileChangedReloadingStrategy {
	    /** Constant for the jar URL protocol.*/
	    private static final String JAR_PROTOCOL = "jar";
	    /** Constant for the JBoss MC VFSFile URL protocol.*/
	    private static final String VFSFILE_PROTOCOL = "vfsfile";

	    @Override
		protected File getFile()
	    {
	        return configuration.getURL() != null ? fileFromURL(configuration
	                .getURL()) : configuration.getFile();
	    }

	    /**
	     * Helper method for transforming a URL into a file object. This method
	     * handles file: and jar: URLs, as well as JBoss VFS-specific vfsfile:
	     * URLs.
	     *
	     * @param url the URL to be converted
	     * @return the resulting file or <b>null </b>
	     */
	    private File fileFromURL(URL url)
	    {
	        if (VFSFILE_PROTOCOL.equals(url.getProtocol()))
	        {
	            String path = url.getPath();
	            try
	            {
	                return ConfigurationUtils.fileFromURL(new URL("file:" + path));
	            }
	            catch (MalformedURLException mex)
	            {
	                return null;
	            }
	        }
	        else if (JAR_PROTOCOL.equals(url.getProtocol()))
	        {
	            String path = url.getPath();
	            try
	            {
	                return ConfigurationUtils.fileFromURL(new URL(path.substring(0,
	                        path.indexOf('!'))));
	            }
	            catch (MalformedURLException mex)
	            {
	                return null;
	            }
	        } else
				return ConfigurationUtils.fileFromURL(url);
	    }
	}
}
