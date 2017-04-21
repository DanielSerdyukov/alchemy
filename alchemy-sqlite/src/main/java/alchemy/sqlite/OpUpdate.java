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

package alchemy.sqlite;

import alchemy.result.CloseableIterator;
import alchemy.source.SourceUpdate;
import alchemy.sqlite.platform.SQLiteTable;

import java.util.Collection;

class OpUpdate<T> implements SourceUpdate<T> {

    private final SQLiteClient mClient;

    private final Collection<T> mObjects;

    OpUpdate(SQLiteClient client, Collection<T> objects) {
        mClient = client;
        mObjects = objects;
    }

    @Override
    public CloseableIterator<T> call() throws Exception {
        if (mObjects.isEmpty()) {
            return Iterators.empty();
        }
        final SQLiteTable<T> table = mClient.resolve(mObjects);
        final long[] ids = mClient.update(table, mObjects);
        return new Mapper<>(mClient, table, mClient.select(table, ids));
    }

    @Override
    public void run() {
        if (!mObjects.isEmpty()) {
            final SQLiteTable<T> table = mClient.resolve(mObjects);
            mClient.update(table, mObjects);
        }
    }

}
