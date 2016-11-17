package rxsqlite;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import rx.functions.Action1;
import rx.functions.Action3;
import sqlite4a.SQLite;
import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTest {

    @Mock
    private SQLiteDriver mDriver;

    @Mock
    private SQLiteDb mNativeDb;

    @Mock
    private RxSQLiteTable mTable;

    private RxSQLiteDbImpl mDb;

    private Connection mConnection;

    @Before
    public void setUp() throws Exception {
        mDb = new RxSQLiteDbImpl(mNativeDb);
        Mockito.doReturn(mDb).when(mDriver).open(Mockito.anyString(), Mockito.anyInt());
        mConnection = new Connection(mDriver);
        mConnection.mTables.add(mTable);
    }

    @Test
    public void openInMemory() throws Exception {
        mConnection.openDatabase(false);
        Mockito.verify(mDriver).open(":memory:", SQLite.OPEN_FULLMUTEX | SQLite.OPEN_READONLY);
    }

    @Test
    public void openInMemoryPrimary() throws Exception {
        mConnection.openDatabase(true);
        Mockito.verify(mDriver).open(":memory:", SQLite.OPEN_FULLMUTEX | SQLite.OPEN_CREATE | SQLite.OPEN_READWRITE);
    }

    @Test
    public void openPath() throws Exception {
        mConnection.mDatabasePath = "/path";
        mConnection.mInMemory = false;
        mConnection.openDatabase(false);
        Mockito.verify(mDriver).open(mConnection.mDatabasePath, SQLite.OPEN_NOMUTEX | SQLite.OPEN_READONLY);
    }

    @Test
    public void openPathPrimary() throws Exception {
        mConnection.mDatabasePath = "/path";
        mConnection.mInMemory = false;
        mConnection.openDatabase(true);
        Mockito.verify(mDriver).open(mConnection.mDatabasePath,
                SQLite.OPEN_NOMUTEX | SQLite.OPEN_CREATE | SQLite.OPEN_READWRITE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void doOnOpen() throws Exception {
        final Action1<SQLiteDb> action = Mockito.mock(Action1.class);
        mConnection.mOnOpen.add(action);
        mConnection.openDatabase(true);
        Mockito.verify(action).call(mNativeDb);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void doOnCreate() throws Exception {
        final Action1<SQLiteDb> action = Mockito.mock(Action1.class);
        mConnection.mOnCreate.add(action);
        final RxSQLiteDbImpl db = mConnection.openDatabase(true);
        Mockito.verify(mTable).create(db);
        Mockito.verify(action).call(mNativeDb);
        Mockito.verify(mNativeDb).setUserVersion(mConnection.mDatabaseVersion);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void doOnUpgrade() throws Exception {
        mConnection.mDatabaseVersion = 2;
        Mockito.doReturn(1).when(mNativeDb).getUserVersion();
        final Action3<SQLiteDb, Integer, Integer> action = Mockito.mock(Action3.class);
        mConnection.mOnUpgrade.add(action);
        mConnection.openDatabase(true);
        Mockito.verify(action).call(mNativeDb, 1, 2);
        Mockito.verify(mNativeDb).setUserVersion(mConnection.mDatabaseVersion);
    }

}