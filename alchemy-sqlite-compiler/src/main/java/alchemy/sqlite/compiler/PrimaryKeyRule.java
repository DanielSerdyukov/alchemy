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

import alchemy.annotations.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

class PrimaryKeyRule implements ProcessingRule {

    private final TypeUtils mTypeUtils;

    private final CompileGraph mCompileGraph;

    PrimaryKeyRule(ProcessingEnvironment processingEnv, CompileGraph compileGraph) {
        mTypeUtils = new TypeUtils(processingEnv);
        mCompileGraph = compileGraph;
    }

    @Override
    public void process(Element element) throws Exception {
        final Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement.getAnnotation(Entry.class) == null) {
            throw new ElementException("Class containing @PrimaryKey must be annotated with @Entry", enclosingElement);
        }
        final String fieldName = element.getSimpleName().toString();
        ColumnType columnType = ColumnType.valueOf(mTypeUtils, element.asType());
        if (columnType != ColumnType.INTEGER) {
            throw new IllegalArgumentException("PrimaryKey must be 'long'");
        }
        columnType = ColumnType.PK;
        final ColumnSpec columnSpec = new ColumnSpec((VariableElement) element,
                fieldName, "_id", columnType);
        mCompileGraph.putColumnSpec(enclosingElement, columnSpec);
    }

}
