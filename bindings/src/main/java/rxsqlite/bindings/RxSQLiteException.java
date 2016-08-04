package rxsqlite.bindings;

/**
 * @author Daniel Serdyukov
 */
public class RxSQLiteException extends RuntimeException {

    public RxSQLiteException(String detailMessage) {
        super(detailMessage);
    }

    public RxSQLiteException(Throwable throwable) {
        super(throwable);
    }

}
