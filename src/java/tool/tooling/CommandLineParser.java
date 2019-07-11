package tool.tooling;

import java.util.ArrayList;
import java.util.List;

public class CommandLineParser {

	private Arg parseArgument(String str) throws CommandLineParsingException {
		if (str.startsWith("-")) {
			Arg arg = null;
			str = str.replaceFirst("^-+", "");
			if (str.contains("=")) {
				int epos = str.indexOf('=');
				arg = new Arg(str.substring(0, epos).toLowerCase(), str.substring(epos+1));
			} else {
				arg = new Arg(str.toLowerCase(), Boolean.TRUE.toString());
			}
			return arg;
		} else {
			return new Arg(null, str);
		}
	}

	public List<Arg> parseArguments(String[] argv) throws CommandLineParsingException {
		List<Arg> arguments = new ArrayList<Arg>();
		for (String arg : argv) {
			arguments.add(parseArgument(arg));
		}
		return arguments;
	}

}
