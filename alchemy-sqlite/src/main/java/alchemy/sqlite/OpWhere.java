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

import alchemy.query.SortBy;
import alchemy.result.CloseableIterator;
import alchemy.source.SourceWhere;
import alchemy.sqlite.platform.SQLiteIterator;
import alchemy.sqlite.platform.SQLiteTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

class OpWhere<T> implements SourceWhere<T> {

    private static final String EQ = " = ?";

    private static final String NEQ = " <> ?";

    private static final String LT = " < ?";

    private static final String LTE = " <= ?";

    private static final String GT = " > ?";

    private static final String GTE = " >= ?";

    private static final String BW = " BETWEEN ? AND ?";

    private static final String LIKE = " LIKE ?";

    private final StringBuilder mWhere = new StringBuilder();

    private final List<Object> mValues = new ArrayList<>();

    private final StringBuilder mOrderBy = new StringBuilder();

    private final SQLiteClient mClient;

    private final Class<T> mClazz;

    OpWhere(SQLiteClient client, Class<T> clazz) {
        mClient = client;
        mClazz = clazz;
    }

    @Override
    public void equalTo(String column, long value) {
        appendWhere(column, EQ, value);
    }

    @Override
    public void equalTo(String column, double value) {
        appendWhere(column, EQ, value);
    }

    @Override
    public void equalTo(String column, byte[] value) {
        appendWhere(column, EQ, value);
    }

    @Override
    public void equalTo(String column, boolean value) {
        appendWhere(column, EQ, value ? 1L : 0L);
    }

    @Override
    public void equalTo(String column, String value) {
        appendWhere(column, EQ, value);
    }

    @Override
    public void equalTo(String column, Date value) {
        appendWhere(column, EQ, value.getTime());
    }

    @Override
    public void notEqualTo(String column, long value) {
        appendWhere(column, NEQ, value);
    }

    @Override
    public void notEqualTo(String column, double value) {
        appendWhere(column, NEQ, value);
    }

    @Override
    public void notEqualTo(String column, byte[] value) {
        appendWhere(column, NEQ, value);
    }

    @Override
    public void notEqualTo(String column, boolean value) {
        appendWhere(column, NEQ, value ? 1L : 0L);
    }

    @Override
    public void notEqualTo(String column, String value) {
        appendWhere(column, NEQ, value);
    }

    @Override
    public void notEqualTo(String column, Date value) {
        appendWhere(column, NEQ, value.getTime());
    }

    @Override
    public void lessThan(String column, long value) {
        appendWhere(column, LT, value);
    }

    @Override
    public void lessThan(String column, double value) {
        appendWhere(column, LT, value);
    }

    @Override
    public void lessThan(String column, Date value) {
        appendWhere(column, LT, value.getTime());
    }

    @Override
    public void lessThanOrEqualTo(String column, long value) {
        appendWhere(column, LTE, value);
    }

    @Override
    public void lessThanOrEqualTo(String column, double value) {
        appendWhere(column, LTE, value);
    }

    @Override
    public void lessThanOrEqualTo(String column, Date value) {
        appendWhere(column, LTE, value.getTime());
    }

    @Override
    public void greaterThan(String column, long value) {
        appendWhere(column, GT, value);
    }

    @Override
    public void greaterThan(String column, double value) {
        appendWhere(column, GT, value);
    }

    @Override
    public void greaterThan(String column, Date value) {
        appendWhere(column, GT, value.getTime());
    }

    @Override
    public void greaterThanOrEqualTo(String column, long value) {
        appendWhere(column, GTE, value);
    }

    @Override
    public void greaterThanOrEqualTo(String column, double value) {
        appendWhere(column, GTE, value);
    }

    @Override
    public void greaterThanOrEqualTo(String column, Date value) {
        appendWhere(column, GTE, value.getTime());
    }

    @Override
    public void between(String column, long from, long to) {
        appendBetween(column, BW, from, to);
    }

    @Override
    public void between(String column, double from, double to) {
        appendBetween(column, BW, from, to);
    }

    @Override
    public void between(String column, Date from, Date to) {
        appendBetween(column, BW, from.getTime(), to.getTime());
    }

    @Override
    public void in(String column, long[] values) {
        appendIn(column, values.length);
        ArrayUtils.addAll(mValues, values);
    }

    @Override
    public void in(String column, double[] values) {
        appendIn(column, values.length);
        ArrayUtils.addAll(mValues, values);
    }

    @Override
    public void in(String column, boolean[] values) {
        appendIn(column, values.length);
        ArrayUtils.addAll(mValues, values);
    }

    @Override
    public void in(String column, String[] values) {
        appendIn(column, values.length);
        Collections.addAll(mValues, values);
    }

    @Override
    public void in(String column, Date[] values) {
        appendIn(column, values.length);
        for (final Date value : values) {
            mValues.add(value.getTime());
        }
    }

    @Override
    public void startsWith(String column, String value) {
        appendWhere(column, LIKE, value + "%");
    }

    @Override
    public void endsWith(String column, String value) {
        appendWhere(column, LIKE, "%" + value);
    }

    @Override
    public void contains(String column, String value) {
        appendWhere(column, LIKE, "%" + value + "%");
    }

    @Override
    public void isNull(String column) {
        mWhere.append(column).append(" IS NULL");
    }

    @Override
    public void notNull(String column) {
        mWhere.append(column).append(" NOT NULL");
    }

    @Override
    public void and() {
        mWhere.append(" AND ");
    }

    @Override
    public void or() {
        mWhere.append(" OR ");
    }

    @Override
    public void beginGroup() {
        mWhere.append("(");
    }

    @Override
    public void endGroup() {
        mWhere.append(")");
    }

    @Override
    public void sortBy(String column, SortBy.Order order) {
        if (mOrderBy.length() > 0) {
            mOrderBy.append(", ");
        }
        mOrderBy.append(column).append(" ").append(order);
    }

    @Override
    public String toString() {
        final StringBuilder selection = new StringBuilder();
        StringUtils.appendNonEmpty(selection, " WHERE ", mWhere.toString());
        StringUtils.appendNonEmpty(selection, " ORDER BY ", mOrderBy.toString());
        return selection.toString();
    }

    @Override
    public CloseableIterator<T> call() {
        final SQLiteTable<T> table = mClient.resolve(mClazz);
        final SQLiteIterator iterator = mClient.select(table, toString(), getValues());
        return new Mapper<>(mClient, table, iterator);
    }

    @Override
    public void run() {
        final SQLiteTable<T> table = mClient.resolve(mClazz);
        mClient.delete(table, toString(), getValues());
    }

    Object[] getValues() {
        return mValues.toArray();
    }

    private void appendWhere(String column, String operator, Object value) {
        mWhere.append(column).append(operator);
        mValues.add(value);
    }

    private void appendBetween(String column, String operator, Object value1, Object value2) {
        mWhere.append(column).append(operator);
        mValues.add(value1);
        mValues.add(value2);
    }

    private void appendIn(String column, int valuesCount) {
        mWhere.append(column).append(" IN(");
        StringUtils.appendNCopies(mWhere, "?", valuesCount, ", ");
        mWhere.append(")");
    }

}
