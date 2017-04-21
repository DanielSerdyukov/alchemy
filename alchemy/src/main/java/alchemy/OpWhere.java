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

import alchemy.query.AlchemyWhere;
import alchemy.query.SortBy;
import alchemy.result.Executable;
import alchemy.result.Fetchable;
import alchemy.source.SourceWhere;

import java.util.Date;

class OpWhere<T> implements AlchemyWhere<T> {

    private final DataSource mSource;

    private final SourceWhere<T> mWhere;

    OpWhere(DataSource source, SourceWhere<T> where) {
        mSource = source;
        mWhere = where;
    }

    @Override
    public AlchemyWhere<T> equalTo(String column, long value) {
        mWhere.equalTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> equalTo(String column, double value) {
        mWhere.equalTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> equalTo(String column, byte[] value) {
        mWhere.equalTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> equalTo(String column, boolean value) {
        mWhere.equalTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> equalTo(String column, String value) {
        mWhere.equalTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> equalTo(String column, Date value) {
        mWhere.equalTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> notEqualTo(String column, long value) {
        mWhere.notEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> notEqualTo(String column, double value) {
        mWhere.notEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> notEqualTo(String column, byte[] value) {
        mWhere.notEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> notEqualTo(String column, boolean value) {
        mWhere.notEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> notEqualTo(String column, String value) {
        mWhere.notEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> notEqualTo(String column, Date value) {
        mWhere.notEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> lessThan(String column, long value) {
        mWhere.lessThan(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> lessThan(String column, double value) {
        mWhere.lessThan(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> lessThan(String column, Date value) {
        mWhere.lessThan(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> lessThanOrEqualTo(String column, long value) {
        mWhere.lessThanOrEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> lessThanOrEqualTo(String column, double value) {
        mWhere.lessThanOrEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> lessThanOrEqualTo(String column, Date value) {
        mWhere.lessThanOrEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> greaterThan(String column, long value) {
        mWhere.greaterThan(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> greaterThan(String column, double value) {
        mWhere.greaterThan(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> greaterThan(String column, Date value) {
        mWhere.greaterThan(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> greaterThanOrEqualTo(String column, long value) {
        mWhere.greaterThanOrEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> greaterThanOrEqualTo(String column, double value) {
        mWhere.greaterThanOrEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> greaterThanOrEqualTo(String column, Date value) {
        mWhere.greaterThanOrEqualTo(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> between(String column, long from, long to) {
        mWhere.between(column, from, to);
        return this;
    }

    @Override
    public AlchemyWhere<T> between(String column, double from, double to) {
        mWhere.between(column, from, to);
        return this;
    }

    @Override
    public AlchemyWhere<T> between(String column, Date from, Date to) {
        mWhere.between(column, from, to);
        return this;
    }

    @Override
    public AlchemyWhere<T> in(String column, long[] values) {
        mWhere.in(column, values);
        return this;
    }

    @Override
    public AlchemyWhere<T> in(String column, double[] values) {
        mWhere.in(column, values);
        return this;
    }

    @Override
    public AlchemyWhere<T> in(String column, boolean[] values) {
        mWhere.in(column, values);
        return this;
    }

    @Override
    public AlchemyWhere<T> in(String column, String[] values) {
        mWhere.in(column, values);
        return this;
    }

    @Override
    public AlchemyWhere<T> in(String column, Date[] values) {
        mWhere.in(column, values);
        return this;
    }

    @Override
    public AlchemyWhere<T> startsWith(String column, String value) {
        mWhere.startsWith(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> endsWith(String column, String value) {
        mWhere.endsWith(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> contains(String column, String value) {
        mWhere.contains(column, value);
        return this;
    }

    @Override
    public AlchemyWhere<T> and() {
        mWhere.and();
        return this;
    }

    @Override
    public AlchemyWhere<T> or() {
        mWhere.or();
        return this;
    }

    @Override
    public AlchemyWhere<T> beginGroup() {
        mWhere.beginGroup();
        return this;
    }

    @Override
    public AlchemyWhere<T> endGroup() {
        mWhere.endGroup();
        return this;
    }

    @Override
    public SortBy<T> sortBy(String column, Order order) {
        mWhere.sortBy(column, order);
        return this;
    }

    @Override
    public Fetchable<T> fetch() {
        return new FetchableResult<>(mWhere);
    }

    @Override
    public Executable delete() {
        return new ExecutableResult(mWhere);
    }

}
