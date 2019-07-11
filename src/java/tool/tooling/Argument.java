package tool.tooling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {
	String name();
	String description() default "<INSERT DESCRIPTION HERE>";
	Class type() default DefaultTypeMapper.class;
	boolean noname() default false;
}
