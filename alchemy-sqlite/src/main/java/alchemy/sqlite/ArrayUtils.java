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

import java.util.Collection;

class ArrayUtils {

    static void addAll(Collection<Object> collection, long[] array) {
        for (long value : array) {
            collection.add(value);
        }
    }

    static void addAll(Collection<Object> collection, double[] array) {
        for (double value : array) {
            collection.add(value);
        }
    }

    static void addAll(Collection<Object> collection, boolean[] array) {
        for (boolean value : array) {
            collection.add(value ? 1L : 0L);
        }
    }

}
