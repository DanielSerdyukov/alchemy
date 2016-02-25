package rxsqlite;

import android.content.Context;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import sqlite4a.SQLiteDb;

/**
 * @author Daniel Serdyukov
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
public class RxSQLiteClientBuilderTest {

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);
    }

    @Test
    public void testMemoryBuilder() throws Exception {
        final SQLiteDb db = Mockito.mock(SQLiteDb.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.memory()
                .registerCustomType(Mockito.mock(RxSQLiteType.class))
                .build());
        Mockito.doReturn(0).when(client).getDatabaseVersion(db);
        Mockito.doReturn(db).when(client).openDatabase(Mockito.anyString(), Mockito.anyInt());

        client.openAndConfigureDatabase();
        Mockito.verify(client).openDatabase("file::memory:?cache=shared", SQLiteDb.OPEN_READWRITE
                | SQLiteDb.OPEN_CREATE | SQLiteDb.OPEN_URI);
    }

    @Test
    public void testContextBuilder() throws Exception {
        final Context context = Mockito.mock(Context.class);
        final File databaseDir = Mockito.mock(File.class);
        Mockito.when(databaseDir.mkdirs()).thenReturn(true);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        Mockito.when(databasePath.getAbsolutePath()).thenReturn("/mock/main.db");
        Mockito.doReturn(databasePath).when(context).getDatabasePath(Mockito.anyString());

        final SQLiteDb db = Mockito.mock(SQLiteDb.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.builder(context, 1).build());
        Mockito.doReturn(0).when(client).getDatabaseVersion(db);
        Mockito.doReturn(db).when(client).openDatabase(Mockito.anyString(), Mockito.anyInt());

        client.openAndConfigureDatabase();
        Mockito.verify(client).openDatabase("/mock/main.db", SQLiteDb.OPEN_READWRITE | SQLiteDb.OPEN_CREATE);
    }

    @Test
    public void testFileBuilder() throws Exception {
        final File databaseDir = Mockito.mock(File.class);
        Mockito.when(databaseDir.mkdirs()).thenReturn(true);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        Mockito.when(databasePath.getAbsolutePath()).thenReturn("/mock/main.db");

        final SQLiteDb db = Mockito.mock(SQLiteDb.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient
                .builder(databasePath, SQLiteDb.OPEN_READONLY, 1).build());
        Mockito.doReturn(0).when(client).getDatabaseVersion(db);
        Mockito.doReturn(db).when(client).openDatabase(Mockito.anyString(), Mockito.anyInt());

        client.openAndConfigureDatabase();
        Mockito.verify(client).openDatabase("/mock/main.db", SQLiteDb.OPEN_READONLY);
    }

    @Test
    public void testFileBuilderWithoutFlags() throws Exception {
        final File databaseDir = Mockito.mock(File.class);
        Mockito.when(databaseDir.exists()).thenReturn(true);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        Mockito.when(databasePath.getAbsolutePath()).thenReturn("/mock/main.db");

        final SQLiteDb db = Mockito.mock(SQLiteDb.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.builder(databasePath, 1).build());
        Mockito.doReturn(0).when(client).getDatabaseVersion(db);
        Mockito.doReturn(db).when(client).openDatabase(Mockito.anyString(), Mockito.anyInt());

        client.openAndConfigureDatabase();
        Mockito.verify(client).openDatabase("/mock/main.db", SQLiteDb.OPEN_READWRITE | SQLiteDb.OPEN_CREATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnsureDatabasePathExists() throws Exception {
        final File databaseDir = Mockito.mock(File.class);
        final File databasePath = Mockito.mock(File.class);
        Mockito.when(databasePath.getParentFile()).thenReturn(databaseDir);
        RxSQLiteClient.ensureDatabasePathExists(databasePath);
    }

}
