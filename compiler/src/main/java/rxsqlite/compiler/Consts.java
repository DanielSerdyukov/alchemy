package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Serdyukov
 */
class Consts {

    static final String PACKAGE = "rxsqlite";

    static final TypeVariableName T_VAR = TypeVariableName.get("T");

    static final ClassName ABSTRACT_TABLE = ClassName.bestGuess(PACKAGE + ".AbstractTable");

    static final ClassName SQLITE_SCHEMA = ClassName.bestGuess(PACKAGE + ".SQLite$$Schema");

    static final ClassName R_TABLE = ClassName.bestGuess(PACKAGE + ".SQLiteTable");

    static final ClassName R_DB = ClassName.bestGuess("rxsqlite.bindings.RxSQLiteDb");

    static final ClassName R_STMT = ClassName.bestGuess("rxsqlite.bindings.RxSQLiteStmt");

    static final ClassName R_CURSOR = ClassName.bestGuess("rxsqlite.bindings.RxSQLiteCursor");

    static final ClassName J_CLASS = ClassName.get(Class.class);

    static final TypeName J_CLASS_W = wildcard(ClassName.get(Class.class));

    static final ClassName J_STRING = ClassName.get(String.class);

    static final ClassName J_COLLECTIONS = ClassName.get(Collections.class);

    static final ClassName J_LIST = ClassName.get(List.class);

    static final ClassName J_ARRAY_LIST = ClassName.get(ArrayList.class);

    static final ClassName J_HASH_MAP = ClassName.get(HashMap.class);

    static TypeName generic(ClassName className, String... types) {
        final TypeName[] generics = new TypeName[types.length];
        for (int i = 0; i < types.length; ++i) {
            generics[i] = TypeVariableName.get(types[i]);
        }
        return ParameterizedTypeName.get(className, generics);
    }

    static TypeName generic(ClassName className, TypeName... types) {
        return ParameterizedTypeName.get(className, types);
    }

    static TypeName mapOf(TypeName key, TypeName value) {
        return ParameterizedTypeName.get(ClassName.get(Map.class), key, value);
    }

    static TypeName wildcard(ClassName className) {
        return ParameterizedTypeName.get(className, TypeVariableName.get("?"));
    }

}
