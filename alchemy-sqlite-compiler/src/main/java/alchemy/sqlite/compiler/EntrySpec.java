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
import javax.lang.model.type.TypeMirror;
import java.util.*;

class EntrySpec {

    private final Set<ColumnSpec> mColumnSpecs = new LinkedHashSet<>();

    private final Set<RelationSpec> mRelationSpecs = new LinkedHashSet<>();

    private final TypeElement mElement;

    private final ClassName mClassName;

    private ColumnSpec mPrimaryKey;

    EntrySpec(Element element, ClassName className) {
        mElement = (TypeElement) element;
        mClassName = className;
    }

    void addColumnSpec(ColumnSpec columnSpec) {
        if (!mColumnSpecs.add(columnSpec)) {
            throw new ElementException("Duplicate column " + columnSpec, columnSpec.getElement());
        }
        if (ColumnType.PK == columnSpec.getColumnType()) {
            mPrimaryKey = columnSpec;
        }
    }

    void addRelationSpec(RelationSpec relationSpec) {
        mRelationSpecs.add(relationSpec);
    }

    ClassName getClassName() {
        return mClassName;
    }

    Collection<ColumnSpec> getColumnSpecs() {
        return Collections.unmodifiableSet(mColumnSpecs);
    }

    Collection<RelationSpec> getRelationSpecs() {
        return mRelationSpecs;
    }

    void brewJava(Filer filer) throws Exception {
        if (mPrimaryKey == null) {
            throw new IllegalStateException("No such field annotated with @PrimaryKey");
        }
        final ClassName modelName = ClassName.get(mElement);
        final TypeSpec.Builder spec = TypeSpec.classBuilder(mClassName.simpleName())
                .addOriginatingElement(mElement)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(SQLiteEntry.class), modelName));
        for (final RelationSpec relationSpec : mRelationSpecs) {
            spec.addField(makeRelationField(relationSpec));
        }
        spec.addMethod(makeGetId());
        spec.addMethod(makeGetRelations());
        spec.addMethod(makeBind());
        spec.addMethod(makeMap());
        JavaFile.builder(mClassName.packageName(), spec.build())
                .addFileComment("Generated code from Alchemy. Do not modify!")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

    private FieldSpec makeRelationField(RelationSpec relationSpec) {
        return FieldSpec.builder(relationSpec.getInterfaceClassName(), relationSpec.getFieldName())
                .initializer("new $T()", relationSpec.getClassName())
                .build();
    }

    private MethodSpec makeGetId() {
        return MethodSpec.methodBuilder("getId")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.LONG)
                .addParameter(ClassName.get(mElement), "object")
                .addStatement("return object.$L", mPrimaryKey.getFieldName())
                .build();
    }

    private MethodSpec makeGetRelations() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("getRelations")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName
                        .get(ClassName.get(Collection.class), ParameterizedTypeName
                                .get(ClassName.get(SQLiteRelation.class),
                                        ClassName.get(mElement), TypeVariableName.get("?"))
                        ));
        if (mRelationSpecs.isEmpty()) {
            builder.addStatement("return $T.emptyList()", Collections.class);
        } else if (mRelationSpecs.size() > 1) {
            builder.addCode("return $T.asList(", Arrays.class);
            final Iterator<RelationSpec> iterator = mRelationSpecs.iterator();
            while (iterator.hasNext()) {
                builder.addCode("$N", makeRelationField(iterator.next()));
                if (iterator.hasNext()) {
                    builder.addCode(", ");
                }
            }
            builder.addCode(");\n");
        } else {
            builder.addStatement("return $T.singletonList($N)", Collections.class,
                    makeRelationField(mRelationSpecs.stream().findFirst().orElse(null)));
        }
        return builder.build();
    }

    private MethodSpec makeBind() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addParameter(SQLiteSchema.class, "schema")
                .addParameter(SQLiteStmt.class, "stmt")
                .addParameter(ClassName.get(mElement), "object");
        int index = 1;
        for (final ColumnSpec columnSpec : mColumnSpecs) {
            final String fieldName = columnSpec.getFieldName();
            final ColumnType columnType = columnSpec.getColumnType();
            if (columnType.isNullable()) {
                builder.beginControlFlow("if (object.$L == null)", fieldName);
                builder.addStatement("stmt.bindNull($L)", index);
                builder.nextControlFlow("else");
            }
            if (ColumnType.PK == columnType) {
                builder.beginControlFlow("if (object.$L > 0)", fieldName);
                builder.addStatement("stmt.bindLong($L, object.$L)", index, fieldName);
                builder.nextControlFlow("else");
                builder.addStatement("stmt.bindNull($L)", index);
                builder.endControlFlow();
            } else if (ColumnType.INTEGER == columnType) {
                builder.addStatement("stmt.bindLong($L, object.$L)", index, fieldName);
            } else if (ColumnType.REAL == columnType) {
                builder.addStatement("stmt.bindDouble($L, object.$L)", index, fieldName);
            } else if (ColumnType.TEXT == columnType) {
                builder.addStatement("stmt.bindString($L, object.$L)", index, fieldName);
            } else if (ColumnType.DATE == columnType) {
                builder.addStatement("stmt.bindLong($L, object.$L.getTime())", index, fieldName);
            } else if (ColumnType.BLOB == columnType) {
                builder.addStatement("stmt.bindBlob($L, object.$L)", index, fieldName);
            }
            if (columnType.isNullable()) {
                builder.endControlFlow();
            }
            ++index;
        }
        builder.addStatement("return $L", index);
        return builder.build();
    }

    private MethodSpec makeMap() {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("map")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(mElement))
                .addParameter(SQLiteSchema.class, "schema")
                .addParameter(SQLiteDb.class, "db")
                .addParameter(SQLiteRow.class, "row")
                .addStatement("final $1T object = new $1T()", ClassName.get(mElement));
        int index = 0;
        for (final ColumnSpec columnSpec : mColumnSpecs) {
            final String fieldName = columnSpec.getFieldName();
            final TypeMirror fieldType = columnSpec.getFieldType();
            final ColumnType columnType = columnSpec.getColumnType();
            if (ColumnType.PK == columnType) {
                builder.addStatement("object.$L = row.getColumnLong($L)", fieldName, index);
            } else if (ColumnType.INTEGER == columnType) {
                builder.addStatement("object.$L = ($T) row.getColumnLong($L)", fieldName, fieldType, index);
            } else if (ColumnType.REAL == columnType) {
                builder.addStatement("object.$L = ($T) row.getColumnDouble($L)", fieldName, fieldType, index);
            } else if (ColumnType.TEXT == columnType) {
                builder.addStatement("object.$L = row.getColumnString($L)", fieldName, index);
            } else if (ColumnType.DATE == columnType) {
                builder.addStatement("object.$L = new $T(row.getColumnLong($L))", fieldName, Date.class, index);
            } else if (ColumnType.BLOB == columnType) {
                builder.addStatement("object.$L = row.getColumnBlob($L)", fieldName, index);
            }
            ++index;
        }
        for (final RelationSpec relationSpec : mRelationSpecs) {
            if (relationSpec.isOneToMany()) {
                builder.addStatement("object.$1L = $1L.fetchList(schema, db, object.$2L)",
                        relationSpec.getFieldName(), mPrimaryKey.getFieldName());
            } else {
                builder.addStatement("object.$1L = $1L.fetchOne(schema, db, object.$2L)",
                        relationSpec.getFieldName(), mPrimaryKey.getFieldName());
            }
        }
        builder.addStatement("return object");
        return builder.build();
    }
}
