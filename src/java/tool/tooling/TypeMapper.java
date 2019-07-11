package tool.tooling;

public interface TypeMapper {
	public Object map(String str, Class clazz) throws TypeMappingException;
	public String supportedValues(Class clazz);
}