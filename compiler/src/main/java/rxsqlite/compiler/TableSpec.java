package rxsqlite.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Daniel Serdyukov
 */
class TableSpec {

    final List<String> constraints = new ArrayList<>();

    final List<String> columnNames = new ArrayList<>();

    final List<String> columnDefs = new ArrayList<>();

    final List<String> indexedColumns = new ArrayList<>();

    final Map<String, TypeMirror> fieldTypes = new LinkedHashMap<>();

    final CodeBlock.Builder instantiate = CodeBlock.builder();

    final CodeBlock.Builder create = CodeBlock.builder();

    final CodeBlock.Builder doAfterInsert = CodeBlock.builder();

    final List<MethodSpec> privateMethods = new ArrayList<>();

    String tableName;

    String pkFieldName;

    boolean hasRelations;

    static TableSpec make(Map<TypeElement, TableSpec> specs, TypeElement element) {
        TableSpec tableSpec = specs.get(element);
        if (tableSpec == null) {
            tableSpec = new TableSpec();
            specs.put(element, tableSpec);
        }
        return tableSpec;
    }

    void validate() throws Exception {
        if (pkFieldName == null) {
            throw new IllegalArgumentException("No such field annotated with @SQLitePk");
        }
    }

}
