package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RuntimeUtils {

	public static String executeLinuxDebianCommand(String[] command) throws Exception {
		StringBuilder output = new StringBuilder();
		Process p = Runtime.getRuntime().exec(command);
//		p.waitFor(); // use this might cause a deadlock
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line).append("\n");
		}
		return output.toString();
	}

	public static int getAvailableCores() {
		return Runtime.getRuntime().availableProcessors();
	}
}
