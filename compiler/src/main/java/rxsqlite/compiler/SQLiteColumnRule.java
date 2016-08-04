package rxsqlite.compiler;

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import rxsqlite.annotation.SQLiteColumn;

/**
 * @author Daniel Serdyukov
 */
class SQLiteColumnRule extends AnnotationRule {

    SQLiteColumnRule(ProcessingEnvironment pEnv) {
        super(pEnv);
    }

    @Override
    public TypeElement getOriginType(Element element) {
        return (TypeElement) element.getEnclosingElement();
    }

    @Override
    void process(TableSpec spec, Element element, Map<TypeElement, TableSpec> specs) throws Exception {
        element.accept(this, null); // mark accessible
        final SQLiteColumn annotation = element.getAnnotation(SQLiteColumn.class);
        final String fieldName = element.getSimpleName().toString();
        final TypeMirror fieldType = element.asType();

        spec.fieldTypes.put(fieldName, fieldType);

        String columnName = annotation.value();
        if (Utils.isEmpty(columnName)) {
            columnName = Utils.toUnderScope(Utils.normalize(fieldName));
        }
        spec.columnNames.add(columnName);

        final StringBuilder columnDef = new StringBuilder(columnName).append(" ");
        if (Utils.isLongType(fieldType) || Utils.isBooleanType(fieldType)) {
            columnDef.append("INTEGER");
        } else if (Utils.isDoubleType(fieldType)) {
            columnDef.append("REAL");
        } else if (Utils.isStringType(fieldType)) {
            columnDef.append("TEXT");
        } else if (Utils.isBlobType(fieldType)) {
            columnDef.append("BLOB");
        } else {
            final String type = annotation.type();
            if (!Utils.isEmpty(type)) {
                columnDef.append(type);
            }
        }

        final String constraint = annotation.constraint();
        if (!Utils.isEmpty(constraint)) {
            columnDef.append(" ").append(constraint);
        }

        spec.columnDefs.add(columnDef.toString());

        if (annotation.index()) {
            spec.indexedColumns.add(columnName);
        }
    }

}
