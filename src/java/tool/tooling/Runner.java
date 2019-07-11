package tool.tooling;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Runner {
	private CommandLineParser cmdParser = new CommandLineParser();
	private AnnotationParser annoParser = new AnnotationParser();

	private String general(Object obj) {
		return String.format("Usage: %s <tool> [arguments]\n" +
		                     "       %s help <tool>\n", obj.getClass().getName(), obj.getClass().getName());
	}

	public static String[] shift(String[] arr, int cnt) {
		int len = arr.length - cnt;
		String[] arrnew = new String[len];
		System.arraycopy(arr, cnt, arrnew, 0, len);
		return arrnew;
	}

	public void run(Object obj, String[] args, PrintStream ps) throws ToolingException {
		Map<String, ToolInfo> toolInfoes = annoParser.parseClassAnnotations(obj.getClass());

		if (args.length == 0) {
			ps.println();
			ps.print(general(obj));
			ps.println();
			printTools(toolInfoes, ps);
			ps.println();
			System.exit(-1);
		}
		String toolname = args[0].toLowerCase();
		args = shift(args, 1);
		if ("help".equals(toolname)) {
			if (args.length != 1) {
				ps.println();
				ps.print(general(obj));
				ps.println();
				printTools(toolInfoes, ps);
				ps.println();
				System.exit(-1);
			}
			toolname = args[0];
			if (!toolInfoes.containsKey(toolname)) {
				ps.println();
				ps.println("Unknown tool: " + toolname + "\n\n");
				ps.print(general(obj));
				ps.println();
				printTools(toolInfoes, ps);
				ps.println();
				System.exit(-1);
			}
			ps.println();
			printToolHelp(toolInfoes.get(toolname), ps);
			ps.println();
			System.exit(0);
		}
		if (!toolInfoes.containsKey(toolname)) {
			ps.println();
			ps.println("Unknown tool: " + toolname + "\n\n");
			ps.println();
			ps.print(general(obj));
			ps.println();
			printTools(toolInfoes, ps);
			ps.println();
			System.exit(-1);
		}

		List<Arg> arglist = cmdParser.parseArguments(args);
		runTool(obj, toolInfoes.get(toolname), arglist);
	}

	private static final int COLS = 100;

	private void printToolHelp(ToolInfo info, PrintStream ps) {
		ps.println("NAME");
		{
			String prefix = "       " + info.name + " — ";
			List<String> lines = fit(info.title, COLS-prefix.length());
			for (int i = 0; i < lines.size(); i++) {
				if (i == 0) {
					ps.print(prefix);
				} else {
					ps.print(spaces(prefix.length()));
				}
				ps.println(lines.get(i));
			}
		}
		ps.println();
		ps.println("USAGE");
		ps.println("       " + usage(info));
		ps.println();
		ps.println("DESCRIPTION");
		{
			List<String> lines = fit(info.description, COLS-7);
			for (String line : lines) {
				ps.println("       " + line);
			}
		}
		ps.println();
		ps.println("ARGUMENTS");
		int maxname = 0;
		for (ArgInfo arg : info.args.values()) {
			maxname = Math.max(arg.name.length(), maxname);
		}

		List<ArgInfo> args = new ArrayList<ArgInfo>();
		args.addAll(info.args.values());
		args.addAll(info.nargs);

		for (ArgInfo arg : args) {
			String out = String.format("    %s    ", pad("--" + arg.name, maxname+2));
			int rwidth = out.length();
			int dwidth = Math.max(COLS-rwidth, 20);
			List<String> lines = fit(argdesc(arg), dwidth);
			for (int i = 0; i < lines.size(); i++) {
				if (i != 0) {
					out += "\n";
					out += spaces(rwidth);
				}
				out += lines.get(i);
			}
			ps.println(out);
			ps.println();
		}
	}

	private String argdesc(ArgInfo ai) {
		String desc = ai.description.trim();
		if (ai.defaultable && ai.defaultValue != null) {
			if (desc.charAt(desc.length()-1) != '.') {
				desc += '.';
			}
			desc += " По умолчанию " + ai.defaultValue + ".";
		}
		if (desc.charAt(desc.length()-1) != '.') {
			desc += '.';
		}
		desc += String.format(" (%s)", ai.mapper.supportedValues(ai.clazz));
		return desc;
	}

	private static List<Arg> removeArgs(List<Arg> arglist, String name) {
		List<Arg> args = new ArrayList<Arg>();
		Iterator<Arg> it = arglist.iterator();
		while (it.hasNext()) {
			Arg arg = it.next();
			if (arg.getName().equals(name)) {
				it.remove();
				args.add(arg);
			}
		}
		return args;
	}

	private void runTool(Object obj, ToolInfo toolinfo, List<Arg> arglist) throws ToolingException {
		Object[] objs = mapArguments(toolinfo, arglist);

		try {
			toolinfo.method.invoke(obj, objs);
		} catch (InvocationTargetException exc) {
			throw new ToolingException(exc);
		} catch (IllegalAccessException exc) {
			throw new ToolingException(exc);
		}
	}

	private Object[] mapArguments(ToolInfo toolinfo, List<Arg> arglist) throws ToolingException {
		Object[] objs = new Object[toolinfo.args.size() + toolinfo.nargs.size()];

		for (String name : toolinfo.args.keySet()) {
			ArgInfo info = toolinfo.args.get(name);
			List<Arg> args = removeArgs(arglist, name);

			if (args.isEmpty()) {
				if (info.isMandatory() && !info.isComplex()) {
					throw new ToolingException("Argument not specified: " + name);
				} else {
					Object obj = info.map(info.defaultValue);
					if (info.isComplex()) {
						Object list = Array.newInstance(info.clazz, 1);
						Array.set(list, 0, obj);
						objs[info.pos] = list;
					} else {
						objs[info.pos] = obj;
					}
				}
			} else {
				if (args.size() > 1 && !info.isComplex()) {
					throw new ToolingException("Duplicate argument: " + name);
				}
				if (info.isComplex()) {
					Object list = Array.newInstance(info.clazz, args.size());
					for (int i = 0; i < args.size(); i++) {
						Array.set(list, i, info.map(args.get(i).getValue()));
					}
					objs[info.pos] = list;

				} else {
					objs[info.pos] = info.map(args.get(0).getValue());
				}
			}
		}

		for (Arg arg : arglist) {
			if (arg.getName() != null) {
				throw new ToolingException("Unknown argument: " + arg.getName());
			}
		}

		for (ArgInfo info : toolinfo.nargs) {
			if (arglist.isEmpty()) {
				throw new ToolingException("Argument not specified: " + info.name);
			}
			if (info.isComplex()) {
				Object arr = Array.newInstance(info.clazz, arglist.size());
				for (int i = 0; i < arglist.size(); i++) {
					Array.set(arr, i, info.map(arglist.get(i).getValue()));
				}
				arglist.clear();
				objs[info.pos] = arr;
			} else {
				objs[info.pos] = info.map(arglist.remove(0).getValue());
			}
		}

		return objs;
	}

	private String usage(ToolInfo tinfo) {
		boolean hasOptions = false;
		for (ArgInfo info : tinfo.args.values()) {
			if (info.defaultable) {
				hasOptions = true;
			}
		}

		StringBuilder str = new StringBuilder();

		str.append(tinfo.name);

		if (hasOptions) {
			str.append(" [options]");
		}

		for (ArgInfo info : tinfo.args.values()) {
			if (info.defaultable) {
				continue;
			}

			String s = "--" + info.name + "=<?>";

			if (info.isOptional()) {
				s = "[" + s + "]";
			}
			if (info.complex) {
				if (info.isOptional()) {
					s = "(" + s + ")*";
				} else {
					s = "(" + s + ")+";
				}
			}

			str.append(' ').append(s);
		}

		for (ArgInfo info : tinfo.nargs) {
			str.append(" <" + info.name + ">");
		}

		return str.toString();
	}

	private static void printTools(Map<String, ToolInfo> infoes, PrintStream ps) {
		ps.println("Available tools:");
		int twidth = 0;
		for (ToolInfo info : infoes.values()) {
			twidth = Math.max(twidth, info.name.length());
		}
		for (ToolInfo info : infoes.values()) {
			String out = String.format("    %s    ", pad(info.name, twidth));
			int rwidth = out.length();
			int dwidth = Math.max(100-rwidth, 20);
			List<String> lines = fit(info.title, dwidth);
			for (int i = 0; i < lines.size(); i++) {
				if (i != 0) {
					out += "\n";
					out += spaces(rwidth);
				}
				out += lines.get(i);
			}
			ps.println(out);
		}
	}

	private static String spaces(int count) {
		String out = "";
		for (int j = 0; j < count; j++) {
			out += " ";
		}
		return out;
	}

	private static String pad(String str, int width) {
		while (str.length() < width) {
			str += ' ';
		}
		return str;
	}

	private static List<String> fit(String str, int width) {
		List<String> lines = new ArrayList<String>();
		while (!str.isEmpty()) {
			int len = optimizeCutPoint(str, Math.min(width, str.length()));
			lines.add(str.substring(0, len));
			str = str.substring(len);
		}
		return lines;
	}

	static int optimizeCutPoint(String str, int target) {
		String tmp = "";
		boolean alpha = false;
		List<String> parts = new ArrayList<String>();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			boolean alphaNew = Character.isLetterOrDigit(c);
			if (!alpha && alphaNew && !tmp.isEmpty()) {
				parts.add(tmp);
				tmp = "";
			}
			alpha = alphaNew;
			tmp += c;
		}
		if (!tmp.isEmpty()) {
			parts.add(tmp);
		}

		String result = "";
		while (!parts.isEmpty() && (result.length() + parts.get(0).length() <= target)) {
			result += parts.remove(0);
		}

		if (result.isEmpty()) {
			return target;
		} else {
			return result.length();
		}
	}

}
