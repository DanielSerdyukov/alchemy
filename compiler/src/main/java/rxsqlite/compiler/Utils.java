package rxsqlite.compiler;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Daniel Serdyukov
 */
class Utils {

    private static final List<Pattern> NAMING = Arrays.asList(
            Pattern.compile("^m([A-Z][a-zA-Z0-9]*)$"),
            Pattern.compile("^([a-z][a-zA-Z0-9]*)$")
    );

    static String normalize(String fieldName) {
        for (final Pattern pattern : NAMING) {
            final Matcher matcher = pattern.matcher(fieldName);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return fieldName;
    }

    static boolean equals(Object var0, Object var1) {
        return var0 == var1 || var0 != null && var0.equals(var1);
    }

    static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    static String toUnderScope(String value) {
        return value.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    static String join(Iterable<?> columnDefs, String glue, String decor) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<?> iterator = columnDefs.iterator();
        while (iterator.hasNext()) {
            sb.append(decor).append(iterator.next()).append(decor);
            if (iterator.hasNext()) {
                sb.append(glue);
            }
        }
        return sb.toString();
    }

    static String join(Iterable<?> columnDefs, String glue) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<?> iterator = columnDefs.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(glue);
            }
        }
        return sb.toString();
    }

    static boolean isLongType(TypeMirror type) {
        return TypeKind.LONG == type.getKind()
                || TypeKind.INT == type.getKind()
                || TypeKind.SHORT == type.getKind();
    }

    static boolean isDoubleType(TypeMirror type) {
        return TypeKind.DOUBLE == type.getKind()
                || TypeKind.FLOAT == type.getKind();
    }

    static boolean isBooleanType(TypeMirror type) {
        return TypeKind.BOOLEAN == type.getKind();
    }

    static boolean isStringType(TypeMirror type) {
        return TypeKind.DECLARED == type.getKind() && "java.lang.String".equals(type.toString());
    }

    static boolean isBlobType(TypeMirror type) {
        if (TypeKind.ARRAY == type.getKind()) {
            final ArrayType arrayType = (ArrayType) type;
            return TypeKind.BYTE == arrayType.getComponentType().getKind();
        }
        return false;
    }

}
