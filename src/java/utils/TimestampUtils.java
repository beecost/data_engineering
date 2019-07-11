package utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Methods for dealing with timestamps
 */
public class TimestampUtils {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy_MM_dd");
	private static final TimeZone TIME_ZONE = TimeZone.getDefault();

	public static final long getTimeZoneOffset(long date, long unit) {
		return TIME_ZONE.getOffset(date) / unit;
	}

	public static final long getTimeZoneOffset(long date) {
		return TIME_ZONE.getOffset(date);
	}

	public static String getIsoWithoutUtcRootZone() {
		return getIsoWithoutUtcRootZone(System.currentTimeMillis());
	}

	public static String getIsoWithoutUtcRootZone(long timestamp) {
		Date now = new Date(timestamp);
		return getIsoWithoutUtcRootZone(now);
	}

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	static {
		dateFormat.setTimeZone(TimeZone.getTimeZone("England/London"));
	}

	private static String getIsoWithoutUtcRootZone(Date date) {
		return dateFormat.format(date);
	}

	public static DateTimeFormatter getDateTimeFormat(String pattern) {
		return DateTimeFormat.forPattern(pattern);
	}

	private TimestampUtils() {
	}

	public static void main(String[] args) throws Exception {
		System.out.println(getIsoWithoutUtcRootZone());
		System.out.println(getTimeZoneOffset(System.currentTimeMillis()) / (3600 * 1000));
	}
}
