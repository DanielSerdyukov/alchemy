package rxsqlite;

/**
 * @author Daniel Serdyukov
 */
class SerializedPool implements Pool {

    private final Connection mConnection;

    private volatile RxSQLiteDbImpl mDb;

    SerializedPool(Connection connection) {
        mConnection = connection;
    }

    @Override
    public RxSQLiteDbImpl acquireDatabase(boolean writable) {
        RxSQLiteDbImpl db = mDb;
        if (db == null) {
            synchronized (this) {
                db = mDb;
                if (db == null) {
                    db = mDb = mConnection.openDatabase(true);
                }
            }
        }
        return db;
    }

    @Override
    public void releaseDatabase(RxSQLiteDbImpl db) {

    }

}
