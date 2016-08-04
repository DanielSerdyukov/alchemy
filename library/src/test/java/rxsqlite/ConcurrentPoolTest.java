package rxsqlite;

import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rxsqlite.bindings.RxSQLiteDb;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class ConcurrentPoolTest {

    @Mock
    private SQLiteHelper mHelper;

    private SQLitePool mPool;

    private ExecutorService mService;

    @Before
    public void setUp() throws Exception {
        Mockito.doAnswer(MockAnswers.openDatabase())
                .when(mHelper)
                .openDatabase(Mockito.anyBoolean());
        mPool = new ConcurrentPool(mHelper);
        mService = Executors.newCachedThreadPool();
    }

    @Test
    public void concurrentAccess() throws Exception {
        final Set<RxSQLiteDb> rdb = new CopyOnWriteArraySet<>();
        final Set<RxSQLiteDb> wdb = new CopyOnWriteArraySet<>();
        for (int i = 0; i < 10; ++i) {
            mService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final RxSQLiteDb db = mPool.acquireDatabase(true);
                    wdb.add(db);
                    mPool.releaseDatabase(db);
                    return null;
                }
            });
        }
        for (int i = 0; i < 10; ++i) {
            mService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final RxSQLiteDb db = mPool.acquireDatabase(false);
                    rdb.add(db);
                    mPool.releaseDatabase(db);
                    return null;
                }
            });
        }
        mService.shutdown();
        mService.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertThat(wdb.size(), Is.is(1));
        for (final RxSQLiteDb db : wdb) {
            Assert.assertThat(db, IsNull.notNullValue());
            Assert.assertThat(db.isReadOnly(), Is.is(false));
        }
        Assert.assertThat(rdb.size(), Matchers.greaterThan(0));
        for (final RxSQLiteDb db : rdb) {
            Assert.assertThat(db, IsNull.notNullValue());
            Assert.assertThat(db.isReadOnly(), Is.is(true));
        }
        Mockito.verify(mHelper).openDatabase(true);
    }

}