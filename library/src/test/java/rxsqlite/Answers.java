package rxsqlite;

import android.support.annotation.NonNull;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Daniel Serdyukov
 */
public final class Answers {

    @NonNull
    static Answer<Boolean> stmtStep(final int count) {
        return new Answer<Boolean>() {
            final AtomicInteger mCount = new AtomicInteger(count);

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return mCount.decrementAndGet() >= 0;
            }
        };
    }

    @NonNull
    static Answer<Boolean> isEmpty() {
        return new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                return args[0] == null || ((String) args[0]).isEmpty();
            }
        };
    }

    @NonNull
    @SuppressWarnings("unchecked")
    static Answer<String> joinIterable() {
        return new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                final Object[] args = invocation.getArguments();
                final StringBuilder sb = new StringBuilder();
                final Iterable<Object> tokens = (Iterable<Object>) args[1];
                final Iterator<Object> iterator = tokens.iterator();
                while (iterator.hasNext()) {
                    sb.append(iterator.next());
                    if (iterator.hasNext()) {
                        sb.append(args[0]);
                    }
                }
                return sb.toString();
            }
        };
    }

}
