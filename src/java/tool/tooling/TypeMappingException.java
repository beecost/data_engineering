package tool.tooling;

public class TypeMappingException extends ToolingException {
	private static final long serialVersionUID = 1L;

	public TypeMappingException(Class<?> clazz, String value) {
		super(String.format("Don't now how to map %s to %s", value, clazz));
	}

	public TypeMappingException(Throwable t) {
		super("Couldn't map", t);
	}
}
