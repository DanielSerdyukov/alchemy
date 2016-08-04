package rxsqlite.bindings;

import java.io.Closeable;
import java.util.Comparator;
import java.util.List;

import rx.functions.Action2;

/**
 * @author Daniel Serdyukov
 */
public interface RxSQLiteDb extends Closeable {

    void enableTracing();

    int getUserVersion();

    void setUserVersion(int version);

    void begin();

    void commit();

    void rollback();

    boolean inTransaction();

    boolean isReadOnly();

    void exec(String sql);

    void createCollation(String name, Comparator<String> comparator);

    void createFunction(String name, int numArgs, Action2<RxSQLiteContext, List<RxSQLiteValue>> callback);

    RxSQLiteStmt prepare(String sql);

    @Override
    void close();

}
