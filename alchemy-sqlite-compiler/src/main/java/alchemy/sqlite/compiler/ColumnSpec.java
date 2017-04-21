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

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

class ColumnSpec {

    private final VariableElement mElement;

    private final String mFieldName;

    private final String mColumnName;

    private final ColumnType mColumnType;

    ColumnSpec(VariableElement element, String fieldName, String columnName, ColumnType columnType) {
        mElement = element;
        mFieldName = fieldName;
        mColumnName = columnName;
        mColumnType = columnType;
    }

    Element getElement() {
        return mElement;
    }

    ColumnType getColumnType() {
        return mColumnType;
    }

    String getFieldName() {
        return mFieldName;
    }

    String getColumnName() {
        return mColumnName;
    }

    TypeMirror getFieldType() {
        return mElement.asType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ColumnSpec that = (ColumnSpec) o;
        return mColumnName != null ? mColumnName.equals(that.mColumnName) : that.mColumnName == null;
    }

    @Override
    public int hashCode() {
        return mColumnName != null ? mColumnName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return mColumnName + " " + mColumnType;
    }

}