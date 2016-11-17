package rxsqlite;

/**
 * @author Daniel Serdyukov
 */
class Strings {

    static String joinNCopies(String value, String glue, int count) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, last = count - 1; i < count; ++i) {
            sb.append(value);
            if (i < last) {
                sb.append(glue);
            }
        }
        return sb.toString();
    }

    static void join(StringBuilder sb, long[] items, String glue) {
        for (int i = 0, last = items.length - 1; i <= last; ++i) {
            sb.append(items[i]);
            if (i < last) {
                sb.append(glue);
            }
        }
    }

}
