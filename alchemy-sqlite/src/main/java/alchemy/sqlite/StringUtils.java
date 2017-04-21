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

class StringUtils {

    static void appendNonEmpty(StringBuilder builder, String name, String clause) {
        if (clause != null && !clause.isEmpty()) {
            builder.append(name).append(clause);
        }
    }

    static void appendNCopies(StringBuilder builder, Object value, int count, String glue) {
        for (int i = 0, last = count - 1; i <= last; ++i) {
            builder.append(value);
            if (i < last) {
                builder.append(glue);
            }
        }
    }

    static void append(StringBuilder builder, String[] values, String glue) {
        for (int i = 0, last = values.length - 1; i <= last; ++i) {
            builder.append(values[i]);
            if (i < last) {
                builder.append(glue);
            }
        }
    }

    static void append(StringBuilder builder, long[] values, String glue) {
        for (int i = 0, last = values.length - 1; i <= last; ++i) {
            builder.append(values[i]);
            if (i < last) {
                builder.append(glue);
            }
        }
    }

}
