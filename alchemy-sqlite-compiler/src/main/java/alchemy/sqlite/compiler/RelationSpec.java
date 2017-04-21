/*
 * Copyright (C) 2017 exzogeni.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alchemy.sqlite.compiler;

import alchemy.sqlite.platform.*;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class RelationSpec {

    private final TypeElement mOriginatingElement;

    private final ClassName mClassName;

    private final TableSpec mLTable;

    private final TableSpec mRTable;

    private final boolean mOneToMany;

    private final String mTableName;

    private final String mFieldName;

    RelationSpec(Element field, ClassName className, TableSpec lTable, TableSpec rTable, boolean oneToMany) {
        mOriginatingElement = (TypeElement) field.getEnclosingElement();
        mClassName = className;
        mLTable = lTable;
        mRTable = rTable;
        mOneToMany = oneToMany;
        mTableName = className.simpleName().toLowerCase();
        mFieldName = field.getSimpleName().toString();
    }

    String getFieldName() {
        return mFieldName;
    }

    ClassName getClassName() {
        return mClassName;
    }

    ParameterizedTypeName getInterfaceClassName() {
        return ParameterizedTypeName.get(ClassName.get(SQLiteRelation.class),
                ClassName.get(mOriginatingElement),
                ClassName.get(mRTable.getElement()));
    }

    boolean isOneToMany() {
        return mOneToMany;
    }

    void brewJava(Filer filer) throws Exception {
        final TypeSpec.Builder spec = TypeSpec.classBuilder(mClassName.simpleName())
                .addOriginatingElement(mOriginatingElement)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(getInterfaceClassName())
                .addMethod(makeCreate())
                .addMethod(makeInsert())
                .addMethod(makeFetchOne())
                .addMethod(makeFetchList());
        JavaFile.builder(mClassName.packageName(), spec.build())
                .addFileComment("Generated code from Alchemy. Do not modify!")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

    private MethodSpec makeCreate() {
        return MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(SQLiteDb.class, "db")
                .addStatement("db.exec($S)", "CREATE TABLE IF NOT EXISTS " + mTableName +
                        "(lfk INTEGER REFERENCES " + mLTable.getTableName() +
                        "(_id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                        "rfk INTEGER, " +
                        "UNIQUE(lfk, rfk) ON CONFLICT IGNORE);")
                .build();
    }

    private MethodSpec makeInsert() {
        final ClassName relationClass = ClassName.get(mRTable.getElement());
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("insert")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(SQLiteSchema.class, "schema")
                .addParameter(SQLiteDb.class, "db")
                .addParameter(ClassName.get(mOriginatingElement), "object")
                .addParameter(TypeName.LONG, "id")
                .addStatement("final $T table = schema.getTable($T.class)", ParameterizedTypeName
                        .get(ClassName.get(SQLiteTable.class), relationClass), relationClass);
        if (mOneToMany) {
            builder.addStatement("final $T[] relIds = table.insert(schema, db, object.$L)",
                    TypeName.LONG, mFieldName);
        } else {
            builder.addStatement("final $T[] relIds = table.insert(schema, db, $T.singletonList(object.$L))",
                    TypeName.LONG, Collections.class, mFieldName);
        }
        builder.addStatement("final $T stmt = db.prepare($S + id + $S)", SQLiteStmt.class,
                "INSERT INTO " + mTableName + " VALUES(", ", ?);");
        builder.beginControlFlow("try");
        builder.beginControlFlow("for (final $T relId : relIds)", TypeName.LONG);
        builder.addStatement("stmt.bindLong(1, relId)");
        builder.addStatement("stmt.execute()");
        builder.addStatement("stmt.clearBindings()");
        builder.endControlFlow();
        builder.nextControlFlow("finally");
        builder.addStatement("stmt.close()");
        builder.endControlFlow();
        return builder.build();
    }

    private MethodSpec makeFetchOne() {
        final ClassName relationClass = ClassName.get(mRTable.getElement());
        return MethodSpec.methodBuilder("fetchOne")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(SQLiteSchema.class, "schema")
                .addParameter(SQLiteDb.class, "db")
                .addParameter(TypeName.LONG, "id")
                .returns(relationClass)
                .addStatement("final $T table = schema.getTable($T.class)", ParameterizedTypeName
                        .get(ClassName.get(SQLiteTable.class), relationClass), relationClass)
                .addStatement("final $T stmt = db.prepare($S + table.getName() + $S + id + $S)", SQLiteStmt.class,
                        "SELECT * FROM ",
                        " WHERE _id IN(SELECT rfk FROM " + mTableName + " WHERE lfk = ", ") LIMIT 1;")
                .beginControlFlow("try")
                .addStatement("final $T iterator = stmt.select()", SQLiteIterator.class)
                .beginControlFlow("if (iterator.hasNext())")
                .addStatement("return table.getEntry().map(schema, db, iterator.next())")
                .endControlFlow()
                .addStatement("return null")
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build();
    }

    private MethodSpec makeFetchList() {
        final ClassName relationClass = ClassName.get(mRTable.getElement());
        return MethodSpec.methodBuilder("fetchList")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(SQLiteSchema.class, "schema")
                .addParameter(SQLiteDb.class, "db")
                .addParameter(TypeName.LONG, "id")
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), relationClass))
                .addStatement("final $T table = schema.getTable($T.class)", ParameterizedTypeName
                        .get(ClassName.get(SQLiteTable.class), relationClass), relationClass)
                .addStatement("final $T stmt = db.prepare($S + table.getName() + $S + id + $S);", SQLiteStmt.class,
                        "SELECT * FROM ",
                        " WHERE _id IN(SELECT rfk FROM " + mTableName + " WHERE lfk = ", ");")
                .beginControlFlow("try")
                .addStatement("final $T iterator = stmt.select()", SQLiteIterator.class)
                .addStatement("final $T list = new $T<>()", ParameterizedTypeName.get(
                        ClassName.get(List.class), relationClass), ArrayList.class)
                .beginControlFlow("while (iterator.hasNext())")
                .addStatement("list.add(table.getEntry().map(schema, db, iterator.next()))")
                .endControlFlow()
                .addStatement("return list")
                .nextControlFlow("finally")
                .addStatement("stmt.close()")
                .endControlFlow()
                .build();
    }

}
