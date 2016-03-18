package rxsqlite.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author Daniel Serdyukov
 */
class OneToMany extends OneToOne {

    OneToMany(String tableName, Element field, Element relType) {
        super(tableName, field, relType);
    }

    @Override
    public void brewSaveRelationMethod(TypeSpec.Builder typeSpec) {
        typeSpec.addMethod(MethodSpec.methodBuilder(getSaveMethodName())
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.SQLITE_DB, "db")
                .addParameter(ParameterizedTypeName.get(
                        ClassName.get(Iterable.class),
                        ClassName.get(getRelType().asType())
                ), "rel")
                .addParameter(TypeName.LONG, "pk")
                .beginControlFlow("if (rel == null)")
                .addStatement("return")
                .endControlFlow()
                .addStatement("final $T relIds = new $T(mTypes).blockingSave(db, rel)",
                        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(Long.class)),
                        ClassName.bestGuess(Table.getTableClassName(getRelType())))
                .beginControlFlow("if (!relIds.isEmpty())")
                .addStatement("final $1T stmt = db.prepare(\"INSERT INTO $2L_$3L_rel($2L_id, $3L_id) VALUES(?, ?);\")",
                        Consts.SQLITE_STMT, getTableName(), getRelTableName())
                .beginControlFlow("try")
                .beginControlFlow("for (final $T relId : relIds)", Long.class)
                .addStatement("stmt.clearBindings()")
                .addStatement("stmt.bindLong(1, pk)")
                .addStatement("stmt.bindLong(2, relId)")
                .addStatement("stmt.executeInsert()")
                .endControlFlow()
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .endControlFlow()
                .build());
    }

    @Override
    public void brewQueryRelationMethod(TypeSpec.Builder typeSpec) {
        typeSpec.addMethod(MethodSpec.methodBuilder(getQueryMethodName())
                .addModifiers(Modifier.PRIVATE)
                .addParameter(Consts.SQLITE_DB, "db")
                .addParameter(TypeName.LONG, "pk")
                .returns(ParameterizedTypeName.get(
                        ClassName.get(List.class),
                        ClassName.get(getRelType().asType())
                ))
                .addStatement("final $T objects = new $T<>()", ParameterizedTypeName.get(
                        ClassName.get(List.class),
                        ClassName.get(getRelType().asType())
                ), ClassName.get(ArrayList.class))
                .addStatement("final $1T stmt = db.prepare(\"SELECT $3L.* FROM $3L, $2L_$3L_rel"
                                + " WHERE $3L._id=$2L_$3L_rel.$3L_id AND $2L_$3L_rel.$2L_id = ?;\")",
                        Consts.SQLITE_STMT, getTableName(), getRelTableName())
                .beginControlFlow("try")
                .addStatement("stmt.bindLong(1, pk)")
                .addStatement("final $T cursor = stmt.executeQuery()", Consts.SQLITE_CURSOR)
                .addStatement("final $1T table = new $1T(mTypes)", ClassName.bestGuess(
                        Table.getTableClassName(getRelType())))
                .beginControlFlow("while (cursor.step())")
                .addStatement("objects.add(table.instantiate(db, cursor))")
                .endControlFlow()
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .addStatement("return objects")
                .build());
    }

}
