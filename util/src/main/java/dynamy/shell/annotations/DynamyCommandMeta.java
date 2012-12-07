package dynamy.shell.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)

public @interface DynamyCommandMeta {

	public String namespace();

	public String name();

	public String[] alias() default {};

	public String[] roles() default {};

	public String[] perms() default {};

	public String description() default "";

}
