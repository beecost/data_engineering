package recsys.utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author tunght
 */
public class StringUtils {

	private static final int OBJ_HEADER;
	private static final int ARR_HEADER;
	private static final int INT_FIELDS = 12;
	private static final int OBJ_REF;
	private static final int OBJ_OVERHEAD;
	private static final boolean IS_64_BIT_JVM;

	/**
	 * Class initializations.
	 */
	static {
		// By default we assume 64 bit JVM
		// (defensive approach since we will get
		// larger estimations in case we are not sure)

		boolean is64BitsJVM = true;
		// check the system property "sun.arch.data.model"
		// not very safe, as it might not work for all JVM implementations
		// nevertheless the worst thing that might happen is that the JVM is
		// 32bit
		// but we assume its 64bit, so we will be counting a few extra bytes per
		// string object
		// no harm done here since this is just an approximation.
		String arch = System.getProperty("sun.arch.data.model");
		if (arch != null) {
			if (arch.indexOf("32") != -1) {
				// If exists and is 32 bit then we assume a 32bit JVM
				is64BitsJVM = false;
			}
		}
		IS_64_BIT_JVM = is64BitsJVM;
		// The sizes below are a bit rough as we don't take into account
		// advanced JVM options such as compressed oops
		// however if our calculation is not accurate it'll be a bit over
		// so there is no danger of an out of memory error because of this.
		OBJ_HEADER = IS_64_BIT_JVM ? 16 : 8;
		ARR_HEADER = IS_64_BIT_JVM ? 24 : 12;
		OBJ_REF = IS_64_BIT_JVM ? 8 : 4;
		OBJ_OVERHEAD = OBJ_HEADER + INT_FIELDS + OBJ_REF + ARR_HEADER;
	}

	/**
	 * Estimates the size of a {@link String} object in bytes.
	 *
	 * @param s The string to estimate memory footprint.
	 * @return The <strong>estimated</strong> size in bytes.
	 */
	public static long estimatedSizeOf(String s) {
		return (s.length() * 2) + OBJ_OVERHEAD;
	}

	/*
	 * don't count reference point to string
	 */
	public static long size(String str) {
		return (str.length() * 2) + OBJ_OVERHEAD;
	}

	public static String getStringFromTokens(String[] tokens, int from, int to) {
		return getStringFromTokens(tokens, from, to, ' ');
	}

	public static String getStringFromTokens(Iterable<String> tokens) {
		return getStringFromTokens(tokens, " ");
	}

	public static String getStringFromTokens(Iterable<String> tokens, String split) {
		StringBuilder result = new StringBuilder();
		boolean notFirst = false;
		for (String token : tokens) {
			if (notFirst) {
				result.append(split);
			}
			result.append(token);
			notFirst = true;
		}
		return result.toString();
	}

	public static String getStringFromTokens(String[] tokens) {
		return getStringFromTokens(tokens, 0, tokens.length, ' ');
	}

	public static String getStringFromTokens(String[] tokens, String split) {
		return getStringFromTokens(tokens, 0, tokens.length, split);
	}

	public static String getStringFromTokens(String[] tokens, int from, int to, String split) {
		StringBuilder rez = new StringBuilder();
		for (int i = from; i < to; i++) {
			if (rez.length() > 0) {
				rez.append(split);
			}
			rez.append(tokens[i]);
		}
		return rez.toString();
	}

	public static String getStringFromTokens(String[] tokens, int from, int to, char split) {
		StringBuilder rez = new StringBuilder();
		for (int i = from; i < to; i++) {
			if (rez.length() > 0) {
				rez.append(split);
			}
			rez.append(tokens[i]);
		}
		return rez.toString();
	}

	public static int indexOf(String[] tokens, String token) {
		int index = -1;
		if (tokens != null) {
			for (int i = 0; i < tokens.length; i++) {
				if (token == null) {
					if (tokens[i] == null) {
						index = i;
						break;
					}
				} else if (token.equals(tokens[i])) {
					index = i;
					break;
				}
			}
		}
		return index;
	}

	public static boolean contains(String[] tokens, String token) {
		return indexOf(tokens, token) >= 0;
	}

	public static String getStringFromTokens(String[] tokens, int from) {
		return getStringFromTokens(tokens, from, tokens.length);
	}

	private static String substring(String str, int from, int to) {
		if (from == to) {
			return "";
		}
		return str.substring(from, to);
	}

	public static String[] split(String str, char delim, int max) {
		if (max <= 0) {
			return splitTrim(str, delim);
		}
		String[] tokens = new String[max];
		int id = 0;
		int start = 0;
		int index = str.indexOf(delim);
		while (index >= 0 && id < max) {
			if (id + 1 == max) {
				index = str.length();
			}
			if (start <= index) {
				tokens[id++] = substring(str, start, index);
			}
			start = index + 1;
			index = str.indexOf(delim, start);
		}
		if (start <= str.length() && id < max) {
			tokens[id++] = substring(str, start, str.length());
		}
		if (id < max) {
			return Arrays.copyOf(tokens, id);
		}
		return tokens;
	}

	public static String[] split(String str, String delim, int max) {
		String[] tokens = new String[max];
		int id = 0;
		int start = 0;
		int index = str.indexOf(delim);
		while (index >= 0 && id < max) {
			if (id + 1 == max) {
				index = str.length();
			}
			if (start <= index) {
				tokens[id++] = substring(str, start, index);
			}
			start = index + delim.length();
			index = str.indexOf(delim, start);
		}
		if (start <= str.length() && id < max) {
			tokens[id++] = substring(str, start, str.length());
		}
		if (id < max) {
			return Arrays.copyOf(tokens, id);
		}
		return tokens;
	}

	public static String[] splitTrim(String str, char delim, int max) {
		if (max <= 0) {
			return splitTrim(str, delim);
		}
		String[] tokens = new String[max];
		int id = 0;
		int start = 0;
		int index = str.indexOf(delim);
		while (index >= 0 && id < max) {
			if (id + 1 == max) {
				index = str.length();
			}
			if (start < index) {
				tokens[id++] = substring(str, start, index);
			}
			start = index + 1;
			index = str.indexOf(delim, start);
		}
		if (start < str.length() && id < max) {
			tokens[id++] = substring(str, start, str.length());
		}
		if (id < max) {
			return Arrays.copyOf(tokens, id);
		}
		return tokens;
	}

	public static String[] splitTrim(String str, String delim, int max) {
		if (max <= 0) {
			return splitTrim(str, delim);
		}
		String[] tokens = new String[max];
		int id = 0;
		int start = 0;
		int index = str.indexOf(delim);
		while (index >= 0 && id < max) {
			if (id + 1 == max) {
				index = str.length();
			}
			if (start < index) {
				tokens[id++] = substring(str, start, index);
			}
			start = index + delim.length();
			index = str.indexOf(delim, start);
		}
		if (start < str.length() && id < max) {
			tokens[id++] = substring(str, start, str.length());
		}
		if (id < max) {
			return Arrays.copyOf(tokens, id);
		}
		return tokens;
	}

	public static String[] split(String str, String delim) {
		ArrayList<String> rez = new ArrayList<>();
		int start = 0;
		int index = str.indexOf(delim);
		while (index >= 0) {
			rez.add(substring(str, start, index));
			start = index + delim.length();
			index = str.indexOf(delim, start);
		}
		if (start <= str.length()) {
			rez.add(substring(str, start, str.length()));
		}
		return rez.toArray(new String[0]);
	}

	public static String[] splitTrim(String str, String delim) {
		ArrayList<String> rez = new ArrayList<>();
		int start = 0;
		int index = str.indexOf(delim);
		while (index >= 0) {
			if (start < index) {
				rez.add(substring(str, start, index));
			}
			start = index + delim.length();
			index = str.indexOf(delim, start);
		}
		if (start < str.length()) {
			rez.add(substring(str, start, str.length()));
		}
		return rez.toArray(new String[0]);
	}

	public static String[] split(String str, char delim) {
		ArrayList<String> rez = new ArrayList<>();
		int start = 0;
		int index = str.indexOf(delim);
		while (index >= 0) {
			rez.add(substring(str, start, index));
			start = index + 1;
			index = str.indexOf(delim, start);
		}
		if (start <= str.length()) {
			rez.add(substring(str, start, str.length()));
		}
		return rez.toArray(new String[0]);
	}

	public static String[] splitTrim(String str, char delim) {
		ArrayList<String> rez = new ArrayList<>();
		int start = 0;
		int index = str.indexOf(delim);
		while (index >= 0) {
			if (start < index) {
				rez.add(substring(str, start, index));
			}
			start = index + 1;
			index = str.indexOf(delim, start);
		}
		if (start < str.length()) {
			rez.add(substring(str, start, str.length()));
		}
		return rez.toArray(new String[0]);
	}

	public static String getStringFromObjects(Object delim, Object[] objs) {
		StringBuilder rez = new StringBuilder();
		boolean first = true;
		for (Object obj : objs) {
			if (first) {
				first = false;
			} else {
				rez.append(delim.toString());
			}
			rez.append(obj.toString());
		}
		return rez.toString();
	}

	public static String getField(final String line, final String sparator,
								  final int index, final boolean trim) {
		if (index <= 0) {
			return null;
		}
		int id = 0, len = line.length();
		if (trim) {
			while (line.startsWith(sparator, id)) {
				id += sparator.length();
			}
			while (line.startsWith(sparator, len - sparator.length())) {
				len = len - sparator.length();
			}
		}
		int count = 0, max = len;
		int last = id;
		while (count < index) {
			count++;
			id = line.indexOf(sparator, last);
			if (id > 0 && trim) {
				int next = id + sparator.length();
				while (next <= len && line.startsWith(sparator, next)) {
					id = next;
					next += sparator.length();
				}
			}
			if (id < 0 || id >= max) {
				if (count == index) {
					id = len;
				}
				break;
			} else if (count < index) {
				id += sparator.length();
				last = id;
			}
		}
		String result = id >= last && count == index ? line.substring(last, id) : null;
		return result;
	}

	public static void main(String[] args) {
	}

}
