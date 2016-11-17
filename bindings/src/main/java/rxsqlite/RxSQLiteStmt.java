package rxsqlite;

import java.io.Closeable;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteStmt extends Closeable {

    void clearBindings();

    void bindNull(int index);

    void bindLong(int index, long value);

    void bindDouble(int index, double value);

    void bindString(int index, String value);

    void bindBlob(int index, byte[] value);

    long executeInsert();

    RxSQLiteCursor executeSelect();

    int executeUpdateDelete();

    @Override
    void close();

}
