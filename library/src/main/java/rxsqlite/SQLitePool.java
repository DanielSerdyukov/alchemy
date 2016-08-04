package rxsqlite;

import rxsqlite.bindings.RxSQLiteDb;

/**
 * @author Daniel Serdyukov
 */
interface SQLitePool {

    RxSQLiteDb acquireDatabase(boolean writable);

    void releaseDatabase(RxSQLiteDb db);

}
