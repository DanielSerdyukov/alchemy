package rxsqlite;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daniel Serdyukov
 */
class TestAnswers {

    static Answer<RxSQLiteDbImpl> openDatabase() {
        return new Answer<RxSQLiteDbImpl>() {
            @Override
            public RxSQLiteDbImpl answer(InvocationOnMock invocation) throws Throwable {
                final RxSQLiteDbImpl db = Mockito.mock(RxSQLiteDbImpl.class);
                Mockito.doReturn(!invocation.<Boolean>getArgument(0)).when(db).isReadOnly();
                return db;
            }
        };
    }

    static Answer<Boolean> step(int count) {
        final AtomicInteger step = new AtomicInteger(count);
        return new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return step.decrementAndGet() >= 0;
            }
        };
    }

    static Answer<Object> instantiate(final List<Object> expected) {
        final AtomicInteger step = new AtomicInteger();
        return new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return expected.get(step.getAndIncrement());
            }
        };
    }
}
