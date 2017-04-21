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

package alchemy.source;

import alchemy.query.SortBy;
import alchemy.result.CloseableIterator;

import java.util.Date;
import java.util.concurrent.Callable;

public interface SourceWhere<T> extends Callable<CloseableIterator<T>>, Runnable {

    void equalTo(String column, long value);

    void equalTo(String column, double value);

    void equalTo(String column, byte[] value);

    void equalTo(String column, boolean value);

    void equalTo(String column, String value);

    void equalTo(String column, Date value);

    void notEqualTo(String column, long value);

    void notEqualTo(String column, double value);

    void notEqualTo(String column, byte[] value);

    void notEqualTo(String column, boolean value);

    void notEqualTo(String column, String value);

    void notEqualTo(String column, Date value);

    void lessThan(String column, long value);

    void lessThan(String column, double value);

    void lessThan(String column, Date value);

    void lessThanOrEqualTo(String column, long value);

    void lessThanOrEqualTo(String column, double value);

    void lessThanOrEqualTo(String column, Date value);

    void greaterThan(String column, long value);

    void greaterThan(String column, double value);

    void greaterThan(String column, Date value);

    void greaterThanOrEqualTo(String column, long value);

    void greaterThanOrEqualTo(String column, double value);

    void greaterThanOrEqualTo(String column, Date value);

    void between(String column, long from, long to);

    void between(String column, double from, double to);

    void between(String column, Date from, Date to);

    void in(String column, long[] values);

    void in(String column, double[] values);

    void in(String column, boolean[] values);

    void in(String column, String[] values);

    void in(String column, Date[] values);

    void startsWith(String column, String value);

    void endsWith(String column, String value);

    void contains(String column, String value);

    void isNull(String column);

    void notNull(String column);

    void and();

    void or();

    void beginGroup();

    void endGroup();

    void sortBy(String column, SortBy.Order order);

}
