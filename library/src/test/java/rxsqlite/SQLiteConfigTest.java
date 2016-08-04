package rxsqlite;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import rxsqlite.bindings.RxSQLiteBinding;

/**
 * @author Daniel Serdyukov
 */
@RunWith(MockitoJUnitRunner.class)
public class SQLiteConfigTest {

    @Mock
    private RxSQLiteBinding mBinding;

    @Test
    public void createInMemoryPool() throws Exception {
        Assert.assertThat(SQLiteConfig.memory().createPool(mBinding),
                IsInstanceOf.instanceOf(InMemoryPool.class));
    }

    @Test
    public void createConcurrentPool() throws Exception {
        final File path = Mockito.mock(File.class);
        final File dir = Mockito.mock(File.class);
        Mockito.when(path.getAbsolutePath()).thenReturn("/mock.db");
        Mockito.when(path.getParentFile()).thenReturn(dir);
        Mockito.when(dir.exists()).thenReturn(true);
        Assert.assertThat(SQLiteConfig.create(path).createPool(mBinding),
                IsInstanceOf.instanceOf(ConcurrentPool.class));
    }

}