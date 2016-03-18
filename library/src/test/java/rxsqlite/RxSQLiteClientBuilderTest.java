package rxsqlite;

import android.content.Context;
import android.util.Log;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.Comparator;

import rx.functions.Func1;
import sqlite4a.SQLiteDb;
import sqlite4a.SQLiteFunc;

/**
 * @author Daniel Serdyukov
 */
@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
public class RxSQLiteClientBuilderTest {

    @Mock
    private SQLiteDb mDb;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);
        MockitoAnnotations.initMocks(this);
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

    @Test
    public void testEnableTracing() throws Exception {
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.memory()
                .enableTracing()
                .build());
        client.dispatchDatabaseOpen(mDb);
        Mockito.verify(mDb).enableTracing();
    }

    @Test
    public void testEnableForeignKeySupport() throws Exception {
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.memory()
                .enableForeignKeySupport()
                .build());
        client.dispatchDatabaseOpen(mDb);
        Mockito.verify(mDb).exec("PRAGMA foreign_keys = ON;");
    }

    @Test
    public void testCreateCollation() throws Exception {
        final Comparator collation = Mockito.mock(Comparator.class);
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.memory()
                .createCollation("RU", collation)
                .build());
        client.dispatchDatabaseOpen(mDb);
        Mockito.verify(mDb).createCollation("RU", collation);
    }

    @Test
    public void testCreateFunction() throws Exception {
        final RxSQLiteClient client = Mockito.spy(RxSQLiteClient.memory()
                .createFunction("test", 1, Mockito.mock(Func1.class))
                .build());
        client.dispatchDatabaseOpen(mDb);
        final ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Integer> numArgs = ArgumentCaptor.forClass(Integer.class);
        final ArgumentCaptor<SQLiteFunc> func = ArgumentCaptor.forClass(SQLiteFunc.class);
        Mockito.verify(mDb).createFunction(name.capture(), numArgs.capture(), func.capture());
        Assert.assertThat(name.getValue(), Is.is("test"));
        Assert.assertThat(numArgs.getValue(), Is.is(1));
        Assert.assertThat(func.getValue(), IsInstanceOf.instanceOf(CustomFunc.class));
    }

}
