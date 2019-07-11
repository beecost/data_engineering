package tool.tooling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
	String value() default NULL;
	static final String NULL = "Ganjoowm4EbwybCiv6Lewyidrayd4";
}
