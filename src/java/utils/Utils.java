package utils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils {
	public static String round(double d, int num) {
		return String.format(Locale.US, "%." + num + "f", d);
	}

	public static String round(double d) {
		return String.format(Locale.US, "%.3f", d);
	}

	public static double min(double a, double b) {
		return (a < b) ? a : b;
	}

	public static long double2long(double d) {
		if (d > Long.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		if (d < Long.MIN_VALUE) {
			return Long.MIN_VALUE;
		}
		return (long) d;
	}

	public static int double2int(double d) {
		if (d > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if (d < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}
		return (int) d;
	}

	/**
	 * different in hours between two timestamp
	 * time must be in format of DATE_FORMAT (yyyy_MM_dd_HH_mm)
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static long hourdiff(String time1, String time2) {
		try {
			Date date1 = DATE_FORMAT.parse(time1);
			Date date2 = DATE_FORMAT.parse(time2);

			long diff = date2.getTime() - date1.getTime();
			return TimeUnit.MILLISECONDS.toHours(diff);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static long hourElapsed(String time) {
		try {
			String current = DATE_FORMAT.format(Calendar.getInstance().getTime());
			return hourdiff(time, current);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm");

	/*
	 * get name with date specific as suffix
	 */
	public static String getNameWithDateSuffix(String name) {
		return name + "_" + DATE_FORMAT.format(Calendar.getInstance().getTime());
	}

    /**
     * get current time
     * @return formated String of current time
     */
    public static String getCurrentDate() {
        return DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    /**
     * Check if date1 is after date2 or not
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isAfter(String date1, String date2) {
        try {
            Date d1 = DATE_FORMAT.parse(date1);
            Date d2 = DATE_FORMAT.parse(date2);

            return d1.after(d2);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }


	/*
	 * get name with date specific as suffix
	 */
	public static String getNameWithDateSuffix(String name, boolean zip) {
		if (!zip) {
			return getNameWithDateSuffix(name);
		}
		return name + "_" + DATE_FORMAT.format(Calendar.getInstance().getTime()) + ".gz";
	}

	public static String getStringFromTokens(String[] tokens, int start, int end, StringBuilder cache) {
		if (cache == null) {
			cache = new StringBuilder();
		} else {
			cache.setLength(0);
		}
		for (int i = start; i < end; i++) {
			if (cache.length() > 0) {
				cache.append(' ');
			}
			cache.append(tokens[i]);
		}
		return cache.toString();
	}

	public static String getStringFromTokens(String[] tokens, int start, int end) {
		return getStringFromTokens(tokens, start, end, null);
	}

	public static void toJson(String s, StringBuilder result) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"':
				case '/':
				case '\\':
					result.append('\\').append(c);
					break;
				case '\b':
					result.append("\\b");
					break;
				case '\f':
					result.append("\\f");
					break;
				case '\n':
					result.append("\\n");
					break;
				case '\r':
					result.append("\\r");
					break;
				case '\t':
					result.append("\\t");
					break;
				default:
					result.append(c);
			}
		}
	}

	public static String toJson(String s) {
		StringBuilder result = new StringBuilder(s.length() + 2);
		toJson(s, result);
		return result.toString();
	}

	/**
	 * rewrite String.startswith for speeding up
	 */
	private static boolean startsWith(String s, String prefix) {
		if (s.length() < prefix.length()) {
			return false;
		}
		for (int i = 0; i < prefix.length(); i++) {
			if (prefix.charAt(i) != s.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * www[0-9]*.site -> site
	 */
	public static String normalize(String site) {
		site = site.toLowerCase();
		if (startsWith(site, "www")) {
			int pos = 3;
			while (pos < site.length()
				&& site.charAt(pos) >= '0' && site.charAt(pos) <= '9') {
				pos++;
			}
			if (pos + 1 < site.length() && site.charAt(pos) == '.') {
				return site.substring(pos + 1);
			}
		}
		return site;
	}

	/**
	 * check hash before using default equal function
	 */
	public static boolean equalUsingHash(String str1, String str2) {
		if (str1 == null || str2 == null) {
			return false;
		}
		if (str1.hashCode() != str2.hashCode()) {
			return false;
		}
		return str1.equals(str2);
	}

	/*
	 * get real local host name of running code
	 * ex: searcher1g.dev.itim.vn
	 */
	public static String getLocalHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getCanonicalHostName();
	}

	public static String getLocalHostName(String defaultName) {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			return defaultName;
		}
	}

	/*
	 * get available cores from current OS
	 * use this value for spawning a suitable number of threads (as average, best number is available cores)
	 */
	public static int getAvailableCores() {
		return Runtime.getRuntime().availableProcessors();
	}

	/*
	 * get free heap memory of JVM
	 */
	public static long getFreeMem() {
		return Runtime.getRuntime().freeMemory();
	}

	/*
	 * get map between param and value base on format "param1=value1&param2=value2..."
	 * note that requestPath has url encoding (ansii)
	 */
	public static Map<String, String> getParamsFromRequestPath(String requestPath, int startIndex)
		throws UnexpectedResultException, UnsupportedEncodingException {
		Map<String, String> params = new HashMap<>();
		StringBuilder param = new StringBuilder();
		int lastIndex = requestPath.length() - 1;
		for (int i = startIndex; i <= lastIndex; i++) {
			if (requestPath.charAt(i) == '&') {
				if (param.length() == 0) {
					continue;
				}
				String pair = param.toString();
				int index = pair.indexOf('=');
				if (index < 0) {
					throw new UnexpectedResultException("invalid param '" + pair + "' of request '" + requestPath + "'");
				}
				params.put(pair.substring(0, index), URLDecoder.decode(pair.substring(index + 1), "UTF-8"));
				param.setLength(0);
			} else {
				param.append(requestPath.charAt(i));
				if (i == lastIndex) {
					String pair = param.toString();
					int index = pair.indexOf('=');
					if (index < 0) {
						throw new UnexpectedResultException("invalid param '" + pair + "' of request '" + requestPath + "'");
					}
					params.put(pair.substring(0, index), URLDecoder.decode(pair.substring(index + 1), "UTF-8"));
				}
			}
		}
		return params;
	}


	public static List<String> addAllStringArray(List<String> source, String[] array) {
		for(int i=0; i<array.length; i++) {
			source.add(array[i]);
		}
		return source;
	}

	public static class UnexpectedResultException extends Exception {

		private static final long serialVersionUID = 6486736453905856082L;

		public UnexpectedResultException(String message) {
			super(message);
		}

	}

	public static <T> T instantiate(final String className, final Class<T> type){
		try{
			return type.cast(Class.forName(className).newInstance());
		} catch(final InstantiationException e){
			throw new IllegalStateException(e);
		} catch(final IllegalAccessException e){
			throw new IllegalStateException(e);
		} catch(final ClassNotFoundException e){
			throw new IllegalStateException(e);
		}
	}
}
