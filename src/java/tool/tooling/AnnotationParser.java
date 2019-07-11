package tool.tooling;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;



public class AnnotationParser {

	@SuppressWarnings("unchecked")
	private <T> T getUniqueAnnotation(Annotation[] annos, Class<T> clazz) {
		for (Annotation anno : annos) {
			if (anno.annotationType() == clazz) {
				return (T) anno;
			}
		}
		return null;
	}

	public ArgInfo parseArgumentAnnotations(Class<?> clazz, Annotation[] annos) throws ConfigurationException {
		Argument arg = getUniqueAnnotation(annos, Argument.class);
		if (arg == null) {
			throw new ConfigurationException("No annotation present: " + Argument.class);
		}
		DefaultValue defval = getUniqueAnnotation(annos, DefaultValue.class);

		ArgInfo info = new ArgInfo();
		info.name = arg.name().toLowerCase();
		info.noname = arg.noname();
		info.description = arg.description();
		if (clazz.isArray()) {
			info.clazz = clazz.getComponentType();
			info.complex = true;
		} else {
			info.clazz = clazz;
			info.complex = false;
		}
		if (defval != null) {
			info.defaultable = true;
			info.defaultValue = DefaultValue.NULL.equals(defval.value()) ? null : defval.value();
		} else {
			info.defaultable = false;
			info.defaultValue = null;
		}
		info.mapper = createMapper(arg.type());
		return info;
	}

	public void parseArgumentListAnnotations(Method method, ToolInfo toolinfo) throws ConfigurationException {
		toolinfo.args = new HashMap<String, ArgInfo>();
		toolinfo.nargs = new ArrayList<ArgInfo>();
		int cnt = method.getParameterTypes().length;
		for (int i = 0; i < cnt; i++) {
			ArgInfo info = parseArgumentAnnotations(method.getParameterTypes()[i], method.getParameterAnnotations()[i]);
			info.pos = i;
			if (info.noname) {
				toolinfo.nargs.add(info);
			} else {
				if (toolinfo.args.containsKey(info.name)) {
					throw new ConfigurationException("Duplicate argument name " + info.name);
				}
				toolinfo.args.put(info.name, info);
			}
		}
		checkNamedArguments(toolinfo.args.values());
		checkNonameArguments(toolinfo.nargs);
	}

	private static void checkNonameArguments(Collection<ArgInfo> arglist) throws ConfigurationException {
		for (ArgInfo arg : arglist) {
			if (arg.isComplex()) {
				throw new ConfigurationException("Noname arguments can't be complex: " + arg.name);
			}
			if (arg.defaultable) {
				throw new ConfigurationException("Noname arguments can't have default value: " + arg.name);
			}
		}
	}

	private static void checkNamedArguments(Collection<ArgInfo> arglist) throws ConfigurationException {
		for (ArgInfo arg : arglist) {
			if (arg.defaultable && arg.defaultValue == null && arg.clazz.isPrimitive()) {
				throw new ConfigurationException("Primitive-typed classes can't have null as default value: " + arg.name);
			}
		}
	}


	private TypeMapper createMapper(Class<?> clazz) throws ConfigurationException {
		try {
			return (TypeMapper) clazz.newInstance();
		} catch (IllegalAccessException exc) {
			throw new ConfigurationException(exc.getClass().getCanonicalName() + ": " + clazz);
		} catch (InstantiationException exc) {
			throw new ConfigurationException(exc.getClass().getCanonicalName() + ": " + clazz);
		} catch (ClassCastException exc) {
			throw new ConfigurationException("Not a mapper: " + clazz);
		}
	}

	public Map<String, ToolInfo> parseClassAnnotations(Class<?> clazz) throws ConfigurationException {
		Map<String, ToolInfo> tools = new TreeMap<String, ToolInfo>();
		for (Method method : clazz.getMethods()) {
			Tool tool = getUniqueAnnotation(method.getAnnotations(), Tool.class);
			if (tool == null) {
				continue;
			}

			ToolInfo info = new ToolInfo();
			info.name = tool.name().toLowerCase();
			info.title = tool.title();
			info.description = tool.description();
			parseArgumentListAnnotations(method, info);
			info.method = method;

			if (tools.containsKey(info.name)) {
				throw new ConfigurationException("Duplicate tool name: " + info.name);
			}
			tools.put(info.name, info);
		}
		return tools;
	}
}
