package utils;

import java.util.UUID;

/**
 * Created on 12/11/2015.
 */
public final class RandomUtils {
	private RandomUtils() {
	}

	public static String randomUUID() {
		return UUID.randomUUID().toString();
	}

	private static String randomByCut(String rand, int maxChars) {
		if (rand.length() > maxChars) {
			rand = rand.substring(0, maxChars);
		}
		return rand;
	}

	public static String randomUUID(int maxChars) {
		String rand = randomUUID();
		return randomByCut(rand, maxChars);
	}

	public static String randomShortUUID() {
		String rand = randomUUID();
		int id = rand.lastIndexOf('-');
		if (id > 0 && id < rand.length() - 1) {
			return rand.substring(id + 1);
		}
		return rand;
	}

	public static String randomUserId() {
		String rand = randomUUID();
		StringBuilder uid = new StringBuilder(rand.length());
		for (int i = 0; i < rand.length(); i++) {
			if (rand.charAt(i) != '-') {
				uid.append(rand.charAt(i));
			}
		}
		return uid.toString();
	}

	public static void main(String[] args) throws Exception {
		System.out.println(randomUserId());
	}

}
