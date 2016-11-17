package rxsqlite.compiler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import rxsqlite.annotation.SQLiteColumn;

/**
 * @author Daniel Serdyukov
 */
class SQLiteColumnRule implements AnnotationRule {

    private static final List<Pattern> NAMING = Arrays.asList(
            Pattern.compile("^m([A-Z][a-zA-Z0-9]*)$"),
            Pattern.compile("^([a-z][a-zA-Z0-9]*)$")
    );

    static String toColumnName(String fieldName) {
        for (final Pattern pattern : NAMING) {
            final Matcher matcher = pattern.matcher(fieldName);
            if (matcher.matches()) {
                return toUnderScope(matcher.group(1));
            }
        }
        return toUnderScope(fieldName);
    }

    static String toUnderScope(String value) {
        return value.replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase();
    }

    @Override
    public void process(Map<TypeElement, TableSpec> specs, Element element) throws Exception {
        final TableSpec tableSpec = TableSpec.get(specs, (TypeElement) element.getEnclosingElement());
        final TypeMirror fieldType = element.asType();
        final StringBuilder columnDef = new StringBuilder();
        final SQLiteColumn annotation = element.getAnnotation(SQLiteColumn.class);
        final String columnType = annotation.type();
        if (!columnType.isEmpty()) {
            columnDef.append(columnType.toUpperCase());
        } else if (Literals.isLong(fieldType) || Literals.isInt(fieldType) || Literals.isShort(fieldType)) {
            columnDef.append("INTEGER");
        } else if (Literals.isDouble(fieldType) || Literals.isFloat(fieldType)) {
            columnDef.append("REAL");
        } else if (Literals.isByteArray(fieldType)) {
            columnDef.append("BLOB");
        } else if (Literals.isDate(fieldType)) {
            columnDef.append("INTEGER");
        } else {
            columnDef.append("TEXT");
        }
        String columnName = annotation.value();
        if (columnName.isEmpty()) {
            columnName = toColumnName(element.getSimpleName().toString());
        }
        final String constraints = annotation.constraints();
        if (!constraints.isEmpty()) {
            columnDef.append(" ").append(constraints);
        }
        tableSpec.addColumn(new ColumnSpec(element, columnName, columnDef.toString()));
        Literals.fixFieldAccess(element);
    }

}
