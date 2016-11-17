package rxsqlite.annotation;

/**
 * @author Daniel Serdyukov
 */
public @interface SQLiteRelation {

    boolean cascade() default true;

}
