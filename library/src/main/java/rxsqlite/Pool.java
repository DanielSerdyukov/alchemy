package rxsqlite;

/**
 * @author Daniel Serdyukov
 */
interface Pool {

    RxSQLiteDbImpl acquireDatabase(boolean writable);

    void releaseDatabase(RxSQLiteDbImpl db);

}
