package gr.grnet.dep.server.jaxrs.util;

import gr.grnet.dep.service.model.User;

import java.util.Date;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

// Customized {@code ContextResolver} implementation to pass ObjectMapper to use
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {

	private ObjectMapper objectMapper;

	public JacksonContextResolver() throws Exception {
		this.objectMapper = new ObjectMapper()
			.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false)
			.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
		this.objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector());

	}

	public ObjectMapper getContext(Class<?> objectType) {
		return objectMapper;
	}
}
