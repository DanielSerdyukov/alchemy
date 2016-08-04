package rxsqlite;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import rxsqlite.bindings.RxSQLiteDb;

/**
 * @author Daniel Serdyukov
 */
class MockAnswers {

    public static Answer<RxSQLiteDb> openDatabase() {
        return new Answer<RxSQLiteDb>() {
            @Override
            public RxSQLiteDb answer(InvocationOnMock invocation) throws Throwable {
                final RxSQLiteDb db = Mockito.mock(RxSQLiteDb.class);
                Mockito.doReturn(!invocation.<Boolean>getArgument(0)).when(db).isReadOnly();
                return db;
            }
        };
    }

    public static Answer<RxSQLiteDb> openDatabase(final int version) {
        return new Answer<RxSQLiteDb>() {
            @Override
            public RxSQLiteDb answer(InvocationOnMock invocation) throws Throwable {
                final RxSQLiteDb db = Mockito.mock(RxSQLiteDb.class);
                if ((invocation.<Integer>getArgument(1) & SQLiteHelper.OPEN_READONLY) != 0) {
                    Mockito.doReturn(true).when(db).isReadOnly();
                }
                Mockito.doReturn(version).when(db).getUserVersion();
                return db;
            }
        };
    }

    public static Answer<Long> sequence(long initialValue) {
        final AtomicLong sequence = new AtomicLong(initialValue);
        return new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) throws Throwable {
                return sequence.getAndIncrement();
            }
        };
    }

    public static Answer<Boolean> steps(int count) {
        final AtomicInteger steps = new AtomicInteger(count);
        return new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return steps.decrementAndGet() >= 0;
            }
        };
    }

    public static Answer<Object> instantiate(final Collection<Object> objects) {
        return new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Object object = new Object();
                objects.add(object);
                return object;
            }
        };
    }

}
