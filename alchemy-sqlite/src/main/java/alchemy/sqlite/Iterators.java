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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Daniel Serdyukov
 */
class Iterators {

    private static final CloseableIterator<Object> EMPTY_ITERATOR = new EmptyIterator<>();

    @SuppressWarnings("unchecked")
    static <T> Class<T> getType(Iterator<T> iterator) {
        if (iterator.hasNext()) {
            return (Class<T>) iterator.next().getClass();
        }
        throw new NoSuchElementException();
    }

    @SuppressWarnings("unchecked")
    static <E> CloseableIterator<E> empty() {
        return (CloseableIterator<E>) EMPTY_ITERATOR;
    }

}
