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

package alchemy;

import alchemy.query.AlchemyDelete;
import alchemy.query.AlchemyInsert;
import alchemy.query.AlchemyUpdate;
import alchemy.query.AlchemyWhere;

import java.util.Collection;
import java.util.Collections;

public class Alchemy {

    private final DataSource mSource;

    public Alchemy(DataSource source) {
        mSource = source;
    }

    public <T> AlchemyWhere<T> where(Class<T> clazz) {
        return new OpWhere<>(mSource, mSource.where(clazz));
    }

    public <T> AlchemyInsert<T> insert(T object) {
        return insert(Collections.singletonList(object));
    }

    public <T> AlchemyInsert<T> insert(Collection<T> objects) {
        return new OpInsert<>(mSource.insert(objects));
    }

    public <T> AlchemyUpdate<T> update(T object) {
        return update(Collections.singletonList(object));
    }

    public <T> AlchemyUpdate<T> update(Collection<T> objects) {
        return new OpUpdate<>(mSource.update(objects));
    }

    public <T> AlchemyDelete delete(T object) {
        return delete(Collections.singletonList(object));
    }

    public <T> AlchemyDelete delete(Collection<T> objects) {
        return new OpDelete(mSource.delete(objects));
    }

}
