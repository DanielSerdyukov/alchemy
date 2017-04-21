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

import alchemy.source.SourceDelete;
import alchemy.source.SourceInsert;
import alchemy.source.SourceUpdate;
import alchemy.source.SourceWhere;

import java.util.Collection;

public interface DataSource {

    <T> SourceWhere<T> where(Class<T> clazz);

    <T> SourceInsert<T> insert(Collection<T> objects);

    <T> SourceUpdate<T> update(Collection<T> objects);

    <T> SourceDelete delete(Collection<T> objects);

}
