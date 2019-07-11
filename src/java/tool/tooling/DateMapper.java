package tool.tooling;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class DateMapper implements TypeMapper {
	private static final String FMT = "dd.MM.yyyy";

	@Override
	public Object map(String str, Class clazz) throws TypeMappingException {
		if ( str == null )
			return null;
		try {
			return new SimpleDateFormat(FMT).parseObject(str);
		} catch (ParseException exc) {
			throw new TypeMappingException(exc);
		}
	}

	@Override
	public String supportedValues(Class clazz) {
		return FMT;
	}
}