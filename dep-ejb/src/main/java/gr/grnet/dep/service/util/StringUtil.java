package gr.grnet.dep.service.util;

import java.text.DecimalFormat;
import java.util.Locale;

public class StringUtil {

	/**
	 * FIRST CHARACTER KEEPS TONE
	 *
	 * @param s
	 * @param locale
	 * @return
	 */
	public static String toUppercaseNoTones(String s, Locale locale) {
		if (s.isEmpty()) {
			return s;
		}
		if (s.length() == 1) {
			return s.toUpperCase(locale);
		}
		return s.substring(0, 1).toUpperCase(locale) +
				s.substring(1).toUpperCase(locale)
						.replaceAll("Ά", "Α")
						.replaceAll("Έ", "Ε")
						.replaceAll("Ή", "Η")
						.replaceAll("Ί", "Ι")
						.replaceAll("Ύ", "Υ")
						.replaceAll("Ό", "Ο")
						.replaceAll("Ώ", "Ω");
	}

	public static String formatPositionID(Long positionID) {
		DecimalFormat df = new DecimalFormat("00000000000");
		return df.format(positionID);
	}

}
