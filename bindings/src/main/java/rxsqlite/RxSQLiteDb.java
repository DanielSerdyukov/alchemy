package rxsqlite;

import java.io.Closeable;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteDb extends Closeable {

    boolean isReadOnly();

    void begin();

    void commit();

    void rollback();

    boolean inTransaction();

    void exec(String sql);

    RxSQLiteStmt prepare(String sql);

    @Override
    void close();

}
