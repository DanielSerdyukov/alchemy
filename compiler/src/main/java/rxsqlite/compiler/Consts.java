package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;

import rx.Observable;

/**
 * @author Daniel Serdyukov
 */
class Consts {

    static final String PACKAGE_NAME = "rxsqlite";

    static final ClassName RXS_TABLE = ClassName.get(PACKAGE_NAME, "RxSQLiteTable");

    static final ClassName RXS_TYPES = ClassName.get(PACKAGE_NAME, "Types");

    static final ClassName SQLITE_DB = ClassName.get("sqlite4a", "SQLiteDb");

    static final ClassName SQLITE_STMT = ClassName.get("sqlite4a", "SQLiteStmt");

    static final ClassName SQLITE_CURSOR = ClassName.get("sqlite4a", "SQLiteCursor");

    static final ClassName OBSERVABLE = ClassName.get(Observable.class);

}
