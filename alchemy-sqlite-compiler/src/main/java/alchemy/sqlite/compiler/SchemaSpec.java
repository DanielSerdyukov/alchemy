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

import alchemy.AlchemyException;
import alchemy.sqlite.platform.SQLiteSchema;
import alchemy.sqlite.platform.SQLiteTable;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class SchemaSpec {

    private final Map<TypeElement, ClassName> mTables = new HashMap<>();

    private final ClassName mClassName;

    SchemaSpec(String name) {
        mClassName = ClassName.get("alchemy.sqlite", name);
    }

    void putTable(TypeElement element, ClassName className) {
        mTables.put(element, className);
    }

    void brewJava(Filer filer) throws Exception {
        final TypeSpec.Builder spec = TypeSpec.classBuilder(mClassName)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "$S", "unchecked")
                        .build())
                .addSuperinterface(ClassName.get(SQLiteSchema.class))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(makeTablesField())
                .addField(makeVersionField())
                .addStaticBlock(makeStaticInit())
                .addMethod(makeInit())
                .addMethod(makeGetVersion())
                .addMethod(makeGetTable())
                .addMethod(makeGetAllTables());
        JavaFile.builder(mClassName.packageName(), spec.build())
                .addFileComment("Generated code from Alchemy. Do not modify!")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

    private FieldSpec makeTablesField() {
        return FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("?")),
                ParameterizedTypeName.get(ClassName.get(SQLiteTable.class), TypeVariableName.get("?"))),
                "TABLES", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>()", HashMap.class)
                .build();
    }

    private FieldSpec makeVersionField() {
        return FieldSpec.builder(TypeName.INT, "mVersion",
                Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private CodeBlock makeStaticInit() {
        final FieldSpec tablesField = makeTablesField();
        final CodeBlock.Builder builder = CodeBlock.builder();
        for (final Map.Entry<TypeElement, ClassName> entry : mTables.entrySet()) {
            final ClassName modelName = ClassName.get(entry.getKey());
            builder.addStatement("$N.put($T.class, new $T())", tablesField, modelName, entry.getValue());
        }
        return builder.build();
    }

    private MethodSpec makeInit() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "version")
                .addStatement("$N = version", makeVersionField())
                .build();
    }

    private MethodSpec makeGetVersion() {
        return MethodSpec.methodBuilder("getVersion")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return $N", makeVersionField())
                .build();
    }

    private MethodSpec makeGetTable() {
        final ParameterizedTypeName returnType = ParameterizedTypeName.get(
                ClassName.get(SQLiteTable.class),
                TypeVariableName.get("T"));
        return MethodSpec.methodBuilder("getTable")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .addParameter(ParameterizedTypeName
                                .get(ClassName.get(Class.class), TypeVariableName.get("T")),
                        "clazz")
                .returns(returnType)
                .addStatement("final $T table = $N.get(clazz)", ParameterizedTypeName
                                .get(ClassName.get(SQLiteTable.class), TypeVariableName.get("?")),
                        makeTablesField())
                .beginControlFlow("if (table != null)")
                .addStatement("return ($T) table", returnType)
                .endControlFlow()
                .addStatement("throw new $T($S + clazz.getCanonicalName())",
                        AlchemyException.class, "No such table for ")
                .build();
    }

    private MethodSpec makeGetAllTables() {
        return MethodSpec.methodBuilder("getAllTables")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Collection.class), ParameterizedTypeName
                        .get(ClassName.get(SQLiteTable.class), TypeVariableName.get("?"))))
                .addStatement("return $N.values()", makeTablesField())
                .build();
    }

}
