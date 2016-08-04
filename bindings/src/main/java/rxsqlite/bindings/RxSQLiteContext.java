package rxsqlite.bindings;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteContext {

    void resultNull();

    void resultLong(long result);

    void resultString(String result);

    void resultDouble(double result);

    void resultBlob(byte[] result);

}
