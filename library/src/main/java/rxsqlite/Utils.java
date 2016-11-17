package rxsqlite;

import java.util.Iterator;

/**
 * @author Daniel Serdyukov
 */
class Utils {

    static void append(StringBuilder sb, String appendGlue, String value) {
        if (!Utils.isEmpty(value)) {
            sb.append(appendGlue).append(value);
        }
    }

    static void append(StringBuilder sb, String appendGlue, Iterable<?> tokens, String joinGlue) {
        final Iterator<?> iterator = tokens.iterator();
        if (iterator.hasNext()) {
            sb.append(appendGlue);
        }
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(joinGlue);
            }
        }
    }

    static <T> T requireNonNull(T object, String nullMessage) {
        if (object == null) {
            throw new NullPointerException(nullMessage);
        }
        return object;
    }

    private static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

}
