package gr.grnet.dep.service.util;

public class CompareUtil {

	public static boolean equalsIgnoreNull(Object a, Object b) {
		return a == null ? (b == null ? true : false) : (b == null ? false : a
				.equals(b));
	}

}
