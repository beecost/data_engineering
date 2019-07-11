package tool.tooling;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


class ToolInfo {
	String name;
	String title;
	String description;
	Method method;
	Map<String, ArgInfo> args;
	List<ArgInfo> nargs;
}