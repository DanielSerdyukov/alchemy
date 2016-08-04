package rxsqlite;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import rx.functions.Action1;
import rx.functions.Action3;
import rxsqlite.bindings.RxSQLiteBinding;
import rxsqlite.bindings.RxSQLiteDb;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLiteHelperTest {

    @Mock
    private RxSQLiteBinding mBinding;

    @Mock
    private Action1<RxSQLiteDb> mOnOpen;

    @Mock
    private Action1<RxSQLiteDb> mOnCreate;

    @Mock
    private Action3<RxSQLiteDb, Integer, Integer> mOnUpgrade;

    @Test
    public void openReadableDatabase() throws Exception {
        final SQLiteHelper helper = createHelper(0, 1);
        final RxSQLiteDb db = helper.openDatabase(false);
        Assert.assertThat(db.isReadOnly(), Is.is(true));
        Mockito.verify(mBinding).openDatabase("/mock.db",
                SQLiteHelper.OPEN_READONLY | SQLiteHelper.OPEN_NOMUTEX);
        Mockito.verify(mOnOpen).call(db);
        Mockito.verifyZeroInteractions(mOnCreate);
        Mockito.verifyZeroInteractions(mOnUpgrade);
    }

    @Test
    public void openWritableDatabase() throws Exception {
        final SQLiteHelper helper = createHelper(0, 1);
        final RxSQLiteDb db = helper.openDatabase(true);
        Assert.assertThat(db.isReadOnly(), Is.is(false));
        Mockito.verify(mBinding).openDatabase("/mock.db",
                SQLiteHelper.OPEN_CREATE | SQLiteHelper.OPEN_READWRITE | SQLiteHelper.OPEN_NOMUTEX);
        Mockito.verify(mOnOpen).call(db);
        Mockito.verify(mOnCreate).call(db);
        Mockito.verifyZeroInteractions(mOnUpgrade);
        Mockito.verify(db).setUserVersion(1);
    }

    @Test
    public void openAndUpgradeDatabase() throws Exception {
        final SQLiteHelper helper = createHelper(1, 2);
        final RxSQLiteDb db = helper.openDatabase(true);
        Assert.assertThat(db.isReadOnly(), Is.is(false));
        Mockito.verify(mBinding).openDatabase("/mock.db",
                SQLiteHelper.OPEN_CREATE | SQLiteHelper.OPEN_READWRITE | SQLiteHelper.OPEN_NOMUTEX);
        Mockito.verify(mOnOpen).call(db);
        Mockito.verify(mOnUpgrade).call(db, 1, 2);
        Mockito.verifyZeroInteractions(mOnCreate);
        Mockito.verify(db).setUserVersion(2);
    }

    @Test
    public void openMemoryDatabase() throws Exception {
        Mockito.doAnswer(MockAnswers.openDatabase(1))
                .when(mBinding)
                .openDatabase(Mockito.anyString(), Mockito.anyInt());
        final SQLiteHelper helper = SQLiteHelper.create(mBinding, SQLiteConfig.memory());
        final RxSQLiteDb db = helper.openDatabase(true);
        Assert.assertThat(db.isReadOnly(), Is.is(false));
        Mockito.verify(mBinding).openDatabase(":memory:",
                SQLiteHelper.OPEN_CREATE | SQLiteHelper.OPEN_READWRITE | SQLiteHelper.OPEN_FULLMUTEX);
    }

    private SQLiteHelper createHelper(int oldVersion, int newVersion) {
        Mockito.doAnswer(MockAnswers.openDatabase(oldVersion))
                .when(mBinding)
                .openDatabase(Mockito.anyString(), Mockito.anyInt());
        final File path = Mockito.mock(File.class);
        final File dir = Mockito.mock(File.class);
        Mockito.when(path.getAbsolutePath()).thenReturn("/mock.db");
        Mockito.when(path.getParentFile()).thenReturn(dir);
        Mockito.when(dir.exists()).thenReturn(true);
        return SQLiteHelper.create(mBinding, SQLiteConfig.create(path, newVersion)
                .doOnOpen(mOnOpen)
                .doOnCreate(mOnCreate)
                .doOnUpgrade(mOnUpgrade));
    }

}