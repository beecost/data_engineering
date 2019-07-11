package utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;

public class DateTimeUtils {

	public static DateTimeFormatter BIRTHDAY_VN_FORMAT = getDateTimeFormatter("dd/MM/yyyy");
	public static DateTimeFormatter BIRTHDAY_EN_FORMAT = getDateTimeFormatter("MM/dd/yyyy");
	public static final Calendar CALENDAR = Calendar.getInstance();
	public static DateTimeFormatter getDateTimeFormatter(String pattern) {
		return DateTimeFormat.forPattern(pattern);
	}

	public static final int currentYear() {
		return CALENDAR.get(Calendar.YEAR);
	}
}
