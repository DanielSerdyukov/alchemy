package rxsqlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author Daniel Serdyukov
 */
@Target({ElementType.FIELD})
public @interface SQLiteColumn {

    String value() default "";

    String type() default "";

    String constraints() default "";

    boolean index() default false;

}
