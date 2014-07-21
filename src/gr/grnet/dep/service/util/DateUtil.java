package gr.grnet.dep.service.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	/**
	 * Compares dates ignoring time
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int compareDates(Date date1, Date date2) {
		if (date1 == null) {
			date1 = new Date(0);
		}
		if (date2 == null) {
			date2 = new Date(0);
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		int diff = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
		if (diff == 0) {
			diff = cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR);
		}
		return diff;
	}

	public static Date removeTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
