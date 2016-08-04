package rxsqlite.bindings;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteCursor {

    boolean step();

    long getColumnLong(int index);

    double getColumnDouble(int index);

    String getColumnString(int index);

    byte[] getColumnBlob(int index);

}
