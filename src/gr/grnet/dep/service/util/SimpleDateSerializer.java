package gr.grnet.dep.service.util;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateSerializer extends JsonSerializer<Date> {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public void serialize(Date aDate, JsonGenerator aJsonGenerator, SerializerProvider aSerializerProvider) throws IOException, JsonProcessingException {
		if (aDate == null) {
			return;
		}
		aJsonGenerator.writeString(dateFormat.format(aDate));
	}
}
