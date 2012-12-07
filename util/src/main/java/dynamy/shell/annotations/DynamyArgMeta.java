package dynamy.shell.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.PARAMETER, ElementType.METHOD })
public @interface DynamyArgMeta {

	public String name();
	public String[] aliases() default {};
	public boolean required() default false;
	public boolean multiValued() default false;
	public String description() default "";

}
