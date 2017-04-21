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

import alchemy.source.SourceDelete;

import java.util.Collection;

class OpDelete<T> implements SourceDelete {

    private final SQLiteClient mClient;

    private final Collection<T> mObjects;

    OpDelete(SQLiteClient client, Collection<T> objects) {
        mClient = client;
        mObjects = objects;
    }

    @Override
    public void run() {
        if (!mObjects.isEmpty()) {
            mClient.delete(mClient.resolve(mObjects), mObjects);
        }
    }

}
