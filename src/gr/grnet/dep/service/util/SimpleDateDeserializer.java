package gr.grnet.dep.service.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateDeserializer extends JsonDeserializer<Date> {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");


	@Override
	public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		String value = jsonParser.getValueAsString();
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return dateFormat.parse(value);
		} catch (ParseException e) {
			throw new IOException(e);
		}
	}
}
