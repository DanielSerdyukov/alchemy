package rxsqlite;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Daniel Serdyukov
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RxSQLite.class)
public class RxSQLiteStaticTest {

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(RxSQLite.class);
        PowerMockito.when(RxSQLite.requireClientNotNull()).thenCallRealMethod();
    }

    @Test(expected = NullPointerException.class)
    public void testRequireClientNotNull() throws Exception {
        PowerMockito.when(RxSQLite.acquireClient()).thenReturn(null);
        RxSQLite.requireClientNotNull();
    }

}
