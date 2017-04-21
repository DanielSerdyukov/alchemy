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

package alchemy.sqlite4a;

import alchemy.sqlite.platform.SQLiteIterator;
import alchemy.sqlite.platform.SQLiteRow;

class SQLite4aIterator implements SQLiteIterator {

    private final sqlite4a.SQLiteIterator mIterator;

    SQLite4aIterator(sqlite4a.SQLiteIterator iterator) {
        mIterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return mIterator.hasNext();
    }

    @Override
    public SQLiteRow next() {
        return new SQLite4aRow(mIterator.next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        mIterator.close();
    }

}
