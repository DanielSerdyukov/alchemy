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

package alchemy.sqlite.platform;

import java.util.Collection;

public interface SQLiteEntry<T> {

    long getId(T object);

    Collection<SQLiteRelation<T, ?>> getRelations();

    int bind(SQLiteSchema schema, SQLiteStmt stmt, T object);

    T map(SQLiteSchema schema, SQLiteDb db, SQLiteRow row);

}