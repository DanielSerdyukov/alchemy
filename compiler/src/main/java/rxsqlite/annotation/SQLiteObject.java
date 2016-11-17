package rxsqlite.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author Daniel Serdyukov
 */
@Target({ElementType.TYPE})
public @interface SQLiteObject {

    String value();

    String[] constraints() default {};

}
