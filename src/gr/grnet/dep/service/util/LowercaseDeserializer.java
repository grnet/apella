package gr.grnet.dep.service.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

public class LowercaseDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
		if (parser.getText() == null || parser.getText().trim().isEmpty()) {
			return null;
		}
		return parser.getText().toLowerCase();
	}
}
