package rxsqlite;

import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class ConcurrentPoolTest {

    @Mock
    private Connection mConnection;

    private ConcurrentPool mPool;

    private ExecutorService mExecutor;

    @Before
    public void setUp() throws Exception {
        Mockito.doAnswer(TestAnswers.openDatabase()).when(mConnection).openDatabase(Mockito.anyBoolean());
        mPool = new ConcurrentPool(mConnection);
        mExecutor = Executors.newFixedThreadPool(5);
    }

    @Test
    public void acquireDatabase() throws Exception {
        final Set<RxSQLiteDbImpl> writable = new CopyOnWriteArraySet<>();
        final Set<RxSQLiteDbImpl> readable = new CopyOnWriteArraySet<>();
        for (int i = 0; i < 100; ++i) {
            final boolean isReadOnly = i % 2 == 0;
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    final RxSQLiteDbImpl db = mPool.acquireDatabase(isReadOnly);
                    if (db.isReadOnly()) {
                        readable.add(db);
                    } else {
                        writable.add(db);
                    }
                    mPool.releaseDatabase(db);
                }
            });
        }
        mExecutor.shutdown();
        mExecutor.awaitTermination(5, TimeUnit.SECONDS);
        Assert.assertThat(writable, IsCollectionWithSize.hasSize(1));
        Assert.assertThat(readable, Matchers.not(IsEmptyCollection.<RxSQLiteDb>empty()));
    }

}