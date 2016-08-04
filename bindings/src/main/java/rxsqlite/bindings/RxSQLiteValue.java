package rxsqlite.bindings;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteValue {

    long longValue();

    String stringValue();

    double doubleValue();

    byte[] blobValue();

}
