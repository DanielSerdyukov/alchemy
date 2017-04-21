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

import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteEntry;
import alchemy.sqlite.platform.SQLiteRelation;
import alchemy.sqlite.platform.SQLiteSchema;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Iterator;

class TableSpec {

    private final TypeElement mElement;

    private final String mTableName;

    private final EntrySpec mEntrySpec;

    private final ClassName mClassName;

    TableSpec(Element element, String tableName, EntrySpec entrySpec) {
        mElement = (TypeElement) element;
        mTableName = tableName;
        mEntrySpec = entrySpec;
        mClassName = ClassName.get("alchemy.sqlite", element.getSimpleName() + "_Table");
    }

    TypeElement getElement() {
        return mElement;
    }

    String getTableName() {
        return mTableName;
    }

    ClassName brewJava(Filer filer) throws Exception {
        final ClassName modelName = ClassName.get(mElement);
        final TypeSpec.Builder spec = TypeSpec.classBuilder(mClassName.simpleName())
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "TryFinallyCanBeTryWithResources")
                        .build())
                .addModifiers(Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.bestGuess("alchemy.sqlite.AbstractTable"), modelName))
                .addField(makeEntryField())
                .addMethod(makeInit())
                .addMethod(makeGetEntry())
                .addMethod(makeCreate());
        if (!mEntrySpec.getRelationSpecs().isEmpty()) {
            spec.addMethod(makeOnInsert());
        }
        JavaFile.builder(mClassName.packageName(), spec.build())
                .addFileComment("Generated code from Alchemy. Do not modify!")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
        return mClassName;
    }

    private FieldSpec makeEntryField() {
        return FieldSpec.builder(ParameterizedTypeName
                        .get(ClassName.get(SQLiteEntry.class), ClassName.get(mElement)),
                "mEntry", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", mEntrySpec.getClassName())
                .build();
    }

    private MethodSpec makeInit() {
        final MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        builder.addCode("super($S, new $T{", mTableName, ArrayTypeName.of(String.class));
        final Iterator<ColumnSpec> iterator = mEntrySpec.getColumnSpecs().iterator();
        while (iterator.hasNext()) {
            builder.addCode("$S", iterator.next().getColumnName());
            if (iterator.hasNext()) {
                builder.addCode(", ");
            }
        }
        builder.addCode("});\n");
        return builder.build();
    }

    private MethodSpec makeGetEntry() {
        return MethodSpec.methodBuilder("getEntry")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName
                        .get(ClassName.get(SQLiteEntry.class), ClassName.get(mElement)))
                .addStatement("return $N", makeEntryField())
                .build();
    }

    private MethodSpec makeCreate() {
        final StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(mTableName)
                .append("(");
        final Iterator<ColumnSpec> iterator = mEntrySpec.getColumnSpecs().iterator();
        while (iterator.hasNext()) {
            sql.append(iterator.next());
            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }
        sql.append(");");
        return MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(SQLiteDb.class, "db")
                .addStatement("db.exec($S)", sql.toString())
                .beginControlFlow("for (final $T relation : $N().getRelations())",
                        ParameterizedTypeName.get(ClassName.get(SQLiteRelation.class),
                                TypeVariableName.get("?"), TypeVariableName.get("?")),
                        makeGetEntry())
                .addStatement("relation.create(db)")
                .endControlFlow()
                .build();
    }

    private MethodSpec makeOnInsert() {
        return MethodSpec.methodBuilder("onInsert")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PROTECTED)
                .addParameter(SQLiteSchema.class, "schema")
                .addParameter(SQLiteDb.class, "db")
                .addParameter(ClassName.get(mElement), "object")
                .addParameter(TypeName.LONG, "id")
                .beginControlFlow("for (final $T relation : $N().getRelations())",
                        ParameterizedTypeName.get(ClassName.get(SQLiteRelation.class),
                                ClassName.get(mElement),
                                TypeVariableName.get("?")),
                        makeGetEntry())
                .addStatement("relation.insert(schema, db, object, id)")
                .endControlFlow()
                .build();
    }

}
