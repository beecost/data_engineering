package tool.tooling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {
	String name();
	String title() default "<INSERT TITLE HERE>";
	String description() default "<INSERT DESCRIPTION HERE>";
}
