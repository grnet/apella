package gr.grnet.dep.service.util;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class IdentificationDeserializer extends JsonDeserializer<String> {

	@Override
	public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		String value = jsonParser.getValueAsString();
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return replaceAllLatinWithGreek(value);
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
