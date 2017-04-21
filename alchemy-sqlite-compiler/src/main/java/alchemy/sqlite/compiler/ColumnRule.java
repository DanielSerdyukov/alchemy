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

import alchemy.annotations.Column;
import alchemy.annotations.Entry;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ColumnRule implements ProcessingRule {

    static final Pattern HUNGARIAN_NOTATION = Pattern.compile("^m([A-Z][a-zA-Z0-9]*)$");

    private final TypeUtils mTypeUtils;

    private final CompileGraph mCompileGraph;

    ColumnRule(ProcessingEnvironment env, CompileGraph compileGraph) {
        mTypeUtils = new TypeUtils(env);
        mCompileGraph = compileGraph;
    }

    private static String toColumnName(String fieldName) {
        final Matcher matcher = HUNGARIAN_NOTATION.matcher(fieldName);
        if (matcher.matches()) {
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, matcher.group(1));
        }
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }

    @Override
    public void process(Element element) throws Exception {
        final Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement.getAnnotation(Entry.class) == null) {
            throw new ElementException("Class containing @Column must be annotated with @Entry", enclosingElement);
        }
        final String fieldName = element.getSimpleName().toString();
        final Column column = element.getAnnotation(Column.class);
        String columnName = null;
        if (column != null) {
            columnName = column.value();
        }
        if (Strings.isNullOrEmpty(columnName)) {
            columnName = toColumnName(fieldName);
        }
        final TypeMirror fieldType = element.asType();
        final ColumnType columnType = ColumnType.valueOf(mTypeUtils, fieldType);
        final ColumnSpec columnSpec = new ColumnSpec((VariableElement) element, fieldName, columnName, columnType);
        mCompileGraph.putColumnSpec(enclosingElement, columnSpec);
    }

}
