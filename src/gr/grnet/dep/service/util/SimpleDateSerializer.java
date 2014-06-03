package gr.grnet.dep.service.util;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateSerializer extends JsonSerializer<Date> {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public void serialize(Date aDate, JsonGenerator aJsonGenerator, SerializerProvider aSerializerProvider) throws IOException {
		if (aDate == null) {
			return;
		}
		aJsonGenerator.writeString(dateFormat.format(aDate));
	}
}
