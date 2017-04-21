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

public interface SQLiteTable<T> {

    String getName();

    SQLiteEntry<T> getEntry();

    void create(SQLiteDb db);

    long[] insert(SQLiteSchema schema, SQLiteDb db, Collection<T> objects);

    SQLiteIterator select(SQLiteDb db, String selection, Object[] args);

    long[] update(SQLiteSchema schema, SQLiteDb db, Collection<T> objects);

    int delete(SQLiteDb db, String selection, Object[] args);

}
