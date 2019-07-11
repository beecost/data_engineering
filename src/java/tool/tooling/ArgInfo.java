package tool.tooling;


class ArgInfo {
	String name;
	String description;
	Class<?> clazz;
	TypeMapper mapper;
	boolean complex;
	String defaultValue;
	boolean defaultable;
	boolean noname;
	int pos;

	public Object map(String str) throws TypeMappingException {
		return mapper.map(str, clazz);
	}

	public boolean isMandatory() {
		return !defaultable;
	}

	public boolean isOptional() {
		return defaultable;
	}

	public boolean isComplex() {
		return complex;
	}

	public String defaultValue() {
		return defaultValue;
	}

	public boolean isCustomMapper() {
		return !DefaultTypeMapper.class.equals(mapper.getClass());
	}
}