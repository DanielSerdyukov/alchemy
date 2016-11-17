package rxsqlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author Daniel Serdyukov
 */
@Target({ElementType.FIELD})
public @interface SQLitePk {

    String constraints() default "ON CONFLICT REPLACE";

}
