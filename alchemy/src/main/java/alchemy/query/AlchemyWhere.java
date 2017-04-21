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

package alchemy.query;

import alchemy.result.Executable;

import java.util.Date;

public interface AlchemyWhere<T> extends SortBy<T> {

    AlchemyWhere<T> equalTo(String column, long value);

    AlchemyWhere<T> equalTo(String column, double value);

    AlchemyWhere<T> equalTo(String column, byte[] value);

    AlchemyWhere<T> equalTo(String column, boolean value);

    AlchemyWhere<T> equalTo(String column, String value);

    AlchemyWhere<T> equalTo(String column, Date value);

    AlchemyWhere<T> notEqualTo(String column, long value);

    AlchemyWhere<T> notEqualTo(String column, double value);

    AlchemyWhere<T> notEqualTo(String column, byte[] value);

    AlchemyWhere<T> notEqualTo(String column, boolean value);

    AlchemyWhere<T> notEqualTo(String column, String value);

    AlchemyWhere<T> notEqualTo(String column, Date value);

    AlchemyWhere<T> lessThan(String column, long value);

    AlchemyWhere<T> lessThan(String column, double value);

    AlchemyWhere<T> lessThan(String column, Date value);

    AlchemyWhere<T> lessThanOrEqualTo(String column, long value);

    AlchemyWhere<T> lessThanOrEqualTo(String column, double value);

    AlchemyWhere<T> lessThanOrEqualTo(String column, Date value);

    AlchemyWhere<T> greaterThan(String column, long value);

    AlchemyWhere<T> greaterThan(String column, double value);

    AlchemyWhere<T> greaterThan(String column, Date value);

    AlchemyWhere<T> greaterThanOrEqualTo(String column, long value);

    AlchemyWhere<T> greaterThanOrEqualTo(String column, double value);

    AlchemyWhere<T> greaterThanOrEqualTo(String column, Date value);

    AlchemyWhere<T> between(String column, long from, long to);

    AlchemyWhere<T> between(String column, double from, double to);

    AlchemyWhere<T> between(String column, Date from, Date to);

    AlchemyWhere<T> in(String column, long[] values);

    AlchemyWhere<T> in(String column, double[] values);

    AlchemyWhere<T> in(String column, boolean[] values);

    AlchemyWhere<T> in(String column, String[] values);

    AlchemyWhere<T> in(String column, Date[] values);

    AlchemyWhere<T> startsWith(String column, String value);

    AlchemyWhere<T> endsWith(String column, String value);

    AlchemyWhere<T> contains(String column, String value);

    AlchemyWhere<T> and();

    AlchemyWhere<T> or();

    AlchemyWhere<T> beginGroup();

    AlchemyWhere<T> endGroup();

    Executable delete();

}
