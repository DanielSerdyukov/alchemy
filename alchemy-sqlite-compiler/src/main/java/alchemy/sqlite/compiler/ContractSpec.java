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

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collection;

class ContractSpec {

    private final Element mElement;

    private final Collection<ColumnSpec> mColumnSpecs;

    private final ClassName mClassName;

    ContractSpec(Element element, Collection<ColumnSpec> columnSpecs) {
        mElement = element;
        mColumnSpecs = columnSpecs;
        final ClassName modelName = ClassName.get((TypeElement) element);
        mClassName = ClassName.get(modelName.packageName(), modelName.simpleName() + "Contract");
    }

    void brewJava(Filer filer) throws Exception {
        final TypeSpec.Builder spec = TypeSpec.classBuilder(mClassName.simpleName())
                .addOriginatingElement(mElement)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        for (final ColumnSpec columnSpec : mColumnSpecs) {
            final String columnName = CaseFormat.LOWER_UNDERSCORE
                    .to(CaseFormat.UPPER_UNDERSCORE, columnSpec.getColumnName());
            spec.addField(FieldSpec.builder(String.class, columnName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", columnSpec.getColumnName())
                    .build());
        }
        spec.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build());
        JavaFile.builder(mClassName.packageName(), spec.build())
                .addFileComment("Generated code from Alchemy. Do not modify!")
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

}
