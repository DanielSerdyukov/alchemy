package rxsqlite;

import java.util.Iterator;

/**
 * @author Daniel Serdyukov
 */
class Utils {

    static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
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

    static void append(StringBuilder sb, String appendGlue, String value) {
        if (!Utils.isEmpty(value)) {
            sb.append(appendGlue).append(value);
        }
    }

    static void append(StringBuilder sb, long[] values, String glue) {
        for (int i = 0, last = values.length - 1; i <= last; ++i) {
            sb.append(values[i]);
            if (i < last) {
                sb.append(glue);
            }
        }
    }

}
