package rxsqlite;

import sqlite4a.SQLite;

/**
 * @author Daniel Serdyukov
 */
class SQLiteDriver {

    RxSQLiteDbImpl open(String path, int flags) {
        return new RxSQLiteDbImpl(SQLite.open(path, flags));
    }

}
