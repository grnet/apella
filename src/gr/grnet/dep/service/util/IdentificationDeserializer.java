package gr.grnet.dep.service.util;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

public class IdentificationDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JsonProcessingException {
		if (parser.getText() == null || parser.getText().trim().isEmpty()) {
			return null;
		}
		return replaceAllLatinWithGreek(parser.getText());
	}

	private static final char[] latin = {'A', 'B', 'E', 'H', 'I', 'K', 'M', 'N', 'O', 'P', 'T', 'X', 'Y', 'Z', 'o', 'p', 'x', 'a', 'i', 'k', 'v'};

	private static final char[] greek = {'Α', 'Β', 'Ε', 'Η', 'Ι', 'Κ', 'Μ', 'Ν', 'Ο', 'Ρ', 'Τ', 'Χ', 'Υ', 'Ζ', 'ο', 'ρ', 'χ', 'α', 'ι', 'κ', 'ν'};

	private static String replaceAllLatinWithGreek(String string) {
		String allGreek = string;
		for (int i = 0; i < latin.length; i++) {
			allGreek = allGreek.replace(latin[i], greek[i]);
		}
		return allGreek;
	}
}
