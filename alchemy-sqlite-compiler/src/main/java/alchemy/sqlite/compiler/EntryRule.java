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
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

class EntryRule implements ProcessingRule {

    private final CompileGraph mCompileGraph;

    EntryRule(CompileGraph compileGraph) {
        mCompileGraph = compileGraph;
    }

    @Override
    public void process(Element element) throws Exception {
        final Entry entry = element.getAnnotation(Entry.class);
        final String schemaClassName = entry.schemaClassName();
        if (Strings.isNullOrEmpty(schemaClassName)) {
            throw new RuntimeException("schemaClassName is empty");
        }
        String tableName = entry.value();
        if (Strings.isNullOrEmpty(tableName)) {
            tableName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, element.getSimpleName().toString());
        }
        final Element enclosingElement = element.getEnclosingElement();
        if (ElementKind.PACKAGE != enclosingElement.getKind()) {
            throw new RuntimeException("Nested classes not supported as Entry");
        }
        final ClassName modelClass = ClassName.get((TypeElement) element);
        final ClassName entryClassName = ClassName.get(modelClass.packageName(),
                modelClass.simpleName() + "_Entry");
        final EntrySpec entrySpec = new EntrySpec(element, entryClassName);
        final TableSpec tableSpec = new TableSpec(element, tableName, entrySpec);
        mCompileGraph.putEntrySpec(element, entrySpec);
        mCompileGraph.putTableSpec(schemaClassName, tableSpec);
    }

}
