package rxsqlite;

/**
 * @author Daniel Serdyukov
 */
public class RxSQLiteException extends RuntimeException {

    public RxSQLiteException(String s) {
        super(s);
    }

    public RxSQLiteException(Throwable throwable) {
        super(throwable);
    }

}
