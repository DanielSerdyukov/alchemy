package rxsqlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Daniel Serdyukov
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
public @interface SQLiteColumn {

    String value() default "";

    String constraint() default "";

}
