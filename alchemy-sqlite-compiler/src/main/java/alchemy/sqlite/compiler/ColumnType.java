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

import javax.lang.model.type.TypeMirror;

enum ColumnType {

    PK("INTEGER PRIMARY KEY ON CONFLICT REPLACE", false),
    INTEGER("INTEGER", false),
    REAL("REAL", false),
    TEXT("TEXT", true),
    DATE("INTEGER", true),
    BLOB("BLOB", true);

    private final String mSqlType;

    private final boolean mNullable;

    ColumnType(String sqlType, boolean nullable) {
        mSqlType = sqlType;
        mNullable = nullable;
    }

    static ColumnType valueOf(TypeUtils types, TypeMirror type) {
        if (types.isLongFamily(type)) {
            return INTEGER;
        } else if (types.isDoubleFamily(type)) {
            return REAL;
        } else if (types.isString(type)) {
            return TEXT;
        } else if (types.isDate(type)) {
            return DATE;
        } else if (types.isByteArray(type)) {
            return BLOB;
        }
        throw new IllegalArgumentException("Unsupported type " + type);
    }

    boolean isNullable() {
        return mNullable;
    }

    @Override
    public String toString() {
        return mSqlType;
    }

}
