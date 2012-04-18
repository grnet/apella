package gr.grnet.dep.server.jaxrs.util;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
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
		
		// Use Jackson annotations as primary; use JAXB annotation as fallback.
		// See http://wiki.fasterxml.com/JacksonJAXBAnnotations
		AnnotationIntrospector primaryIntrospector = new JacksonAnnotationIntrospector();
	    AnnotationIntrospector secondaryIntropsector = new JaxbAnnotationIntrospector();
	    AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primaryIntrospector, secondaryIntropsector);
		this.objectMapper.setAnnotationIntrospector(pair);

	}

	public ObjectMapper getContext(Class<?> objectType) {
		return objectMapper;
	}
}
