package gr.grnet.dep.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.text.SimpleDateFormat;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonConfig implements ContextResolver<ObjectMapper> {

	private ObjectMapper objectMapper;

	public JacksonConfig() throws Exception {
		Hibernate4Module hibernateModule = new Hibernate4Module()
				.configure(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION, false);

		this.objectMapper = new ObjectMapper()
				.registerModule(hibernateModule)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false)
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.setDateFormat(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"));

	}

	public ObjectMapper getContext(Class<?> objectType) {
		return objectMapper;
	}
}
