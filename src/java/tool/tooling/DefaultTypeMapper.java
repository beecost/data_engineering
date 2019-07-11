package tool.tooling;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class DefaultTypeMapper implements TypeMapper {

	public static String implode(String delim, List<String> elements) {
		return implode(delim, elements.toArray(new String[]{}));
	}

	public static String implode(String delim, String... elements) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for (String el : elements) {
			if (!first) {
				str.append(delim);
			}
			str.append(el);
			first = false;
		}

		return str.toString();
	}

	public DefaultTypeMapper() {
	}

	public Object map(String str, Class clazz) throws TypeMappingException {
		if (str == null) {
			return null;
		} else if (clazz == String.class) {
			return str;
		} else if (clazz == Boolean.class || clazz == Boolean.TYPE) {
			return new Boolean(Boolean.parseBoolean(str));
		} else if (clazz == Byte.class || clazz == Byte.TYPE) {
			return new Byte(Byte.parseByte(str));
		} else if (clazz == Short.class || clazz == Short.TYPE) {
			return new Short(Short.parseShort(str));
		} else if (clazz == Integer.class || clazz == Integer.TYPE) {
			return new Integer(Integer.parseInt(str));
		} else if (clazz == Long.class || clazz == Long.TYPE) {
			return new Long(Long.parseLong(str));
		} else if (clazz == Float.class || clazz == Float.TYPE) {
			return new Float(Float.parseFloat(str));
		} else if (clazz == Double.class || clazz == Double.TYPE) {
			return new Double(Double.parseDouble(str));
		} else if (clazz == Character.class || clazz == Character.TYPE) {
			if (str.length() != 1) {
				throw new TypeMappingException(clazz, str);
			}
			return new Character((char)str.indexOf(0));
		} else if (clazz == File.class) {
			return new File(str);
		} else if (clazz == URL.class) {
			try {
				return new URL(str);
			} catch (MalformedURLException exc) {
				throw new TypeMappingException(clazz, str);
			}
		} else if (clazz == URI.class) {
			try {
				return new URI(str);
			} catch (URISyntaxException exc) {
				throw new TypeMappingException(clazz, str);
			}
		} else if (clazz.isEnum()) {
			try {
				return Enum.valueOf(clazz, str);
			} catch (IllegalArgumentException exc) {
				throw new TypeMappingException(clazz, str);
			}
		} else {
			throw new TypeMappingException(clazz, str);
		}
	}

	@Override
	public String supportedValues(Class clazz) {
		if (clazz.isEnum()) {
			List<?> constants = Arrays.asList(clazz.getEnumConstants());
			List<String> values = new ArrayList<String>(constants.size());
			for (Object constant : constants) {
				values.add(constant.toString());
			}
			return String.format(implode(", ", values));
		} else if (clazz == Boolean.class || clazz == Boolean.TYPE) {
			return "true|false";
		} else {
			return clazz.getSimpleName();
		}
	}
}