package rxsqlite.compiler;

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import rxsqlite.annotation.SQLitePk;

/**
 * @author Daniel Serdyukov
 */
class SQLitePkRule extends SQLiteColumnRule {

    SQLitePkRule(ProcessingEnvironment pEnv) {
        super(pEnv);
    }

    @Override
    void process(TableSpec spec, Element element, Map<TypeElement, TableSpec> specs) throws Exception {
        element.accept(this, null); // mark accessible
        if (TypeKind.LONG != element.asType().getKind()) {
            throw new IllegalArgumentException("PRIMARY KEY must be long");
        }
        final SQLitePk annotation = element.getAnnotation(SQLitePk.class);
        spec.columnNames.add("_id");
        spec.pkFieldName = element.getSimpleName().toString();
        spec.fieldTypes.put(spec.pkFieldName, element.asType());
        String columnDef = "_id INTEGER PRIMARY KEY";
        final String constraint = annotation.constraint();
        if (!Utils.isEmpty(columnDef)) {
            columnDef += " " + constraint;
        }
        spec.columnDefs.add(columnDef);
    }

}
