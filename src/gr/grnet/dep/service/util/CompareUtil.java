package gr.grnet.dep.service.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class CompareUtil {

	public static boolean equalsIgnoreNull(Object a, Object b) {
		return a == null ? (b == null) : (b == null ? false : a.equals(b));
	}

	public static <T> boolean compareCollections(Collection<T> collection1, Collection<T> collection2, Comparator<T> comparator) {
		if (collection1.size() != collection2.size()) {
			return false;
		}
		Set<T> set1 = new TreeSet<T>(comparator);
		set1.addAll(collection1);
		Set<T> set2 = new TreeSet<T>(comparator);
		set2.addAll(collection2);
		return set1.containsAll(set2);
	}

	/**
	 * @param collection1
	 * @param collection2
	 * @param comparator
	 * @return Elements common to Collection2 and Collection1
	 */
	public static <T> Collection<T> intersection(Collection<T> collection1, Collection<T> collection2, Comparator<T> comparator) {
		Set<T> set1 = new TreeSet<T>(comparator);
		set1.addAll(collection1);
		Set<T> set2 = new TreeSet<T>(comparator);
		set2.addAll(collection2);
		Set<T> intersection = new TreeSet<T>(comparator);
		for (T element : set1) {
			if (set2.contains(element)) {
				intersection.add(element);
			}
		}
		return intersection;
	}

	/**
	 * @param collection1
	 * @param collection2
	 * @param comparator
	 * @return Elements in Collection1 not in Collection2
	 */
	public static <T> Collection<T> complement(Collection<T> collection1, Collection<T> collection2, Comparator<T> comparator) {
		Set<T> set1 = new TreeSet<T>(comparator);
		set1.addAll(collection1);
		Set<T> set2 = new TreeSet<T>(comparator);
		set2.addAll(collection2);

		set1.removeAll(set2);
		return set1;
	}
}
