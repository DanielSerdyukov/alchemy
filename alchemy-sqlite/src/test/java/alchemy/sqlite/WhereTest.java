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
import org.hamcrest.collection.IsArrayContainingInOrder;
import org.hamcrest.collection.IsArrayWithSize;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WhereTest {

    @Mock
    private SQLiteClient mClient;

    private OpWhere<Object> mWhere;

    @Before
    public void setUp() throws Exception {
        mWhere = new OpWhere<>(mClient, Object.class);
    }

    @Test
    public void equalToLong() throws Exception {
        mWhere.equalTo("test", 100L);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test = ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(100L));
    }

    @Test
    public void equalToDouble() throws Exception {
        mWhere.equalTo("test", 1.23);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test = ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(1.23));
    }

    @Test
    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public void equalToBlob() throws Exception {
        mWhere.equalTo("test", new byte[]{1, 2, 3});
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test = ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(new byte[]{1, 2, 3}));
    }

    @Test
    public void equalToBoolean() throws Exception {
        mWhere.equalTo("test", true);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test = ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(1L));
    }

    @Test
    public void equalToString() throws Exception {
        mWhere.equalTo("test", "TEST");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test = ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("TEST"));
    }

    @Test
    public void notEqualToLong() throws Exception {
        mWhere.notEqualTo("test", 123L);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test <> ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(123L));
    }

    @Test
    public void notEqualToDouble() throws Exception {
        mWhere.notEqualTo("test", 4.56);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test <> ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(4.56));
    }

    @Test
    @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
    public void notEqualToBlob() throws Exception {
        mWhere.notEqualTo("test", new byte[]{1, 2, 3});
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test <> ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(new byte[]{1, 2, 3}));
    }

    @Test
    public void notEqualToBoolean() throws Exception {
        mWhere.notEqualTo("test", false);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test <> ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(0L));
    }

    @Test
    public void notEqualToString() throws Exception {
        mWhere.notEqualTo("test", "NOT TEST");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test <> ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("NOT TEST"));
    }

    @Test
    public void lessThanLong() throws Exception {
        mWhere.lessThan("test", 234L);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test < ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(234L));
    }

    @Test
    public void lessThanDouble() throws Exception {
        mWhere.lessThan("test", 2.34);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test < ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(2.34));
    }

    @Test
    public void lessThanOrEqualToLong() throws Exception {
        mWhere.lessThanOrEqualTo("test", 345L);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test <= ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(345L));
    }

    @Test
    public void lessThanOrEqualToDouble() throws Exception {
        mWhere.lessThanOrEqualTo("test", 3.45);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test <= ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(3.45));
    }

    @Test
    public void greaterThanLong() throws Exception {
        mWhere.greaterThan("test", 456L);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test > ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(456L));
    }

    @Test
    public void greaterThanDouble() throws Exception {
        mWhere.greaterThan("test", 4.56);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test > ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(4.56));
    }

    @Test
    public void greaterThanOrEqualToLong() throws Exception {
        mWhere.greaterThanOrEqualTo("test", 567L);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test >= ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(567L));
    }

    @Test
    public void greaterThanOrEqualToDouble() throws Exception {
        mWhere.greaterThanOrEqualTo("test", 5.67);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test >= ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(5.67));
    }

    @Test
    public void betweenLong() throws Exception {
        mWhere.between("test", 123L, 456L);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test BETWEEN ? AND ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(123L, 456L));
    }

    @Test
    public void betweenDouble() throws Exception {
        mWhere.between("test", 1.23, 4.56);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test BETWEEN ? AND ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(1.23, 4.56));
    }

    @Test
    public void inLong() throws Exception {
        mWhere.in("test", new long[]{1L, 10L, 100L});
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test IN(?, ?, ?)"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(1L, 10L, 100L));
    }

    @Test
    public void inDouble() throws Exception {
        mWhere.in("test", new double[]{1.1, 10.2, 100.3});
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test IN(?, ?, ?)"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(1.1, 10.2, 100.3));
    }

    @Test
    public void inBoolean() throws Exception {
        mWhere.in("test", new boolean[]{true, true, false});
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test IN(?, ?, ?)"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(1L, 1L, 0L));
    }

    @Test
    public void inString() throws Exception {
        mWhere.in("test", new String[]{"t1", "t2", "t3"});
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test IN(?, ?, ?)"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("t1", "t2", "t3"));
    }

    @Test
    public void startsWith() throws Exception {
        mWhere.startsWith("test", "te");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test LIKE ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("te%"));
    }

    @Test
    public void endsWith() throws Exception {
        mWhere.endsWith("test", "st");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test LIKE ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("%st"));
    }

    @Test
    public void contains() throws Exception {
        mWhere.contains("test", "es");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test LIKE ?"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("%es%"));
    }

    @Test
    public void isNull() throws Exception {
        mWhere.isNull("test");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test IS NULL"));
        Assert.assertThat(mWhere.getValues(), IsArrayWithSize.emptyArray());
    }

    @Test
    public void notNull() throws Exception {
        mWhere.notNull("test");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test NOT NULL"));
        Assert.assertThat(mWhere.getValues(), IsArrayWithSize.emptyArray());
    }

    @Test
    public void and() throws Exception {
        mWhere.equalTo("test1", "test");
        mWhere.and();
        mWhere.lessThanOrEqualTo("test2", 1.23);
        mWhere.and();
        mWhere.notNull("test3");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(
                " WHERE test1 = ? AND test2 <= ? AND test3 NOT NULL"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("test", 1.23));
    }

    @Test
    public void or() throws Exception {
        mWhere.notEqualTo("test1", "test");
        mWhere.or();
        mWhere.greaterThan("test2", 345L);
        mWhere.or();
        mWhere.isNull("test3");
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(
                " WHERE test1 <> ? OR test2 > ? OR test3 IS NULL"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("test", 345L));
    }

    @Test
    public void beginGroup_endGroup() throws Exception {
        mWhere.beginGroup();
        mWhere.equalTo("test", true);
        mWhere.endGroup();
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE (test = ?)"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining(1L));
    }

    @Test
    public void sortByAsc() throws Exception {
        mWhere.sortBy("test", SortBy.Order.ASC);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" ORDER BY test ASC"));
        Assert.assertThat(mWhere.getValues(), IsArrayWithSize.emptyArray());
    }

    @Test
    public void sortByDesc() throws Exception {
        mWhere.sortBy("test", SortBy.Order.DESC);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" ORDER BY test DESC"));
        Assert.assertThat(mWhere.getValues(), IsArrayWithSize.emptyArray());
    }

    @Test
    public void where_sortBy() throws Exception {
        mWhere.equalTo("test1", "test");
        mWhere.sortBy("test2", SortBy.Order.DESC);
        Assert.assertThat(mWhere.toString(), IsEqual.equalTo(" WHERE test1 = ? ORDER BY test2 DESC"));
        Assert.assertThat(mWhere.getValues(), IsArrayContainingInOrder.<Object>arrayContaining("test"));
    }

}