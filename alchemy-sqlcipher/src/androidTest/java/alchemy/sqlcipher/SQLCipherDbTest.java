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

package alchemy.sqlcipher;

import alchemy.sqlite.platform.SQLiteDb;
import alchemy.sqlite.platform.SQLiteIterator;
import alchemy.sqlite.platform.SQLiteRow;
import alchemy.sqlite.platform.SQLiteStmt;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import net.sqlcipher.database.SQLiteDatabase;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.junit.*;
import org.junit.runner.RunWith;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
@TargetApi(Build.VERSION_CODES.KITKAT)
public class SQLCipherDbTest {

    private List<Model> mModels;

    private SQLiteDb mDb;

    @BeforeClass
    public static void loadJni() {
        SQLiteDatabase.loadLibs(InstrumentationRegistry.getContext());
    }

    @Before
    public void setUp() throws Exception {
        mDb = new SQLCipherDriver("test123").open(":memory:", false, false);
        mDb.exec("CREATE TABLE IF NOT EXISTS test(_id INTEGER PRIMARY KEY, uuid TEXT, weight REAL, data BLOB);");
        final Random random = new SecureRandom();
        mModels = new ArrayList<>();
        for (int i = 0; i < 15; ++i) {
            mModels.add(Model.create(random));
        }
        mDb.begin();
        try (SQLiteStmt stmt = mDb.prepare("INSERT INTO test VALUES(?, ?, ?, ?);")) {
            for (final Model model : mModels) {
                stmt.bindNull(1);
                stmt.bindString(2, model.uuid);
                stmt.bindDouble(3, model.weight);
                stmt.bindBlob(4, model.data);
                model.id = stmt.insert();
                stmt.clearBindings();
            }
        }
        mDb.commit();
    }

    @Test
    public void select_empty() throws Exception {
        final SQLiteStmt stmt = mDb.prepare("SELECT * FROM test WHERE _id = 0;");
        try (final SQLiteIterator iterator = stmt.select()) {
            Assert.assertThat(iterator.hasNext(), Is.is(false));
        }
    }

    @Test
    public void select_where_long_eq() throws Exception {
        final Model model = mModels.get(3);
        final SQLiteStmt stmt = mDb.prepare("SELECT * FROM test WHERE _id = ?;");
        stmt.bindLong(1, model.id);
        try (final SQLiteIterator iterator = stmt.select()) {
            Assert.assertThat(iterator.hasNext(), Is.is(true));
            final SQLiteRow row = iterator.next();
            Assert.assertThat(row.getColumnLong(0), Is.is(model.id));
            Assert.assertThat(row.getColumnString(1), IsEqual.equalTo(model.uuid));
            Assert.assertThat(row.getColumnDouble(2), Matchers.closeTo(model.weight, 1e-5));
            Assert.assertThat(row.getColumnBlob(3), IsEqual.equalTo(model.data));
            Assert.assertThat(iterator.hasNext(), Is.is(false));
        }
    }

    @Test
    public void select_where_string_eq() throws Exception {
        final Model model = mModels.get(5);
        final SQLiteStmt stmt = mDb.prepare("SELECT * FROM test WHERE uuid = ?;");
        stmt.bindString(1, model.uuid);
        try (final SQLiteIterator iterator = stmt.select()) {
            Assert.assertThat(iterator.hasNext(), Is.is(true));
            final SQLiteRow row = iterator.next();
            Assert.assertThat(row.getColumnLong(0), Is.is(model.id));
            Assert.assertThat(row.getColumnString(1), IsEqual.equalTo(model.uuid));
            Assert.assertThat(row.getColumnDouble(2), Matchers.closeTo(model.weight, 1e-5));
            Assert.assertThat(row.getColumnBlob(3), IsEqual.equalTo(model.data));
            Assert.assertThat(iterator.hasNext(), Is.is(false));
        }
    }

    @Test
    public void select_where_double_eq() throws Exception {
        final Model model = mModels.get(7);
        final SQLiteStmt stmt = mDb.prepare("SELECT * FROM test WHERE weight = ?;");
        stmt.bindDouble(1, model.weight);
        try (final SQLiteIterator iterator = stmt.select()) {
            Assert.assertThat(iterator.hasNext(), Is.is(true));
            final SQLiteRow row = iterator.next();
            Assert.assertThat(row.getColumnLong(0), Is.is(model.id));
            Assert.assertThat(row.getColumnString(1), IsEqual.equalTo(model.uuid));
            Assert.assertThat(row.getColumnDouble(2), Matchers.closeTo(model.weight, 1e-5));
            Assert.assertThat(row.getColumnBlob(3), IsEqual.equalTo(model.data));
            Assert.assertThat(iterator.hasNext(), Is.is(false));
        }
    }

    @Test
    public void select_where_blob_eq() throws Exception {
        final Model model = mModels.get(9);
        final SQLiteStmt stmt = mDb.prepare("SELECT * FROM test WHERE data = ?;");
        stmt.bindBlob(1, model.data);
        try (final SQLiteIterator iterator = stmt.select()) {
            Assert.assertThat(iterator.hasNext(), Is.is(true));
            final SQLiteRow row = iterator.next();
            Assert.assertThat(row.getColumnLong(0), Is.is(model.id));
            Assert.assertThat(row.getColumnString(1), IsEqual.equalTo(model.uuid));
            Assert.assertThat(row.getColumnDouble(2), Matchers.closeTo(model.weight, 1e-5));
            Assert.assertThat(row.getColumnBlob(3), IsEqual.equalTo(model.data));
            Assert.assertThat(iterator.hasNext(), Is.is(false));
        }
    }


    @Test
    public void rollback_transaction() throws Exception {
        int expectedCount = 0;
        final SQLiteIterator beforeInsert = mDb.prepare("SELECT * FROM test;").select();
        while (beforeInsert.hasNext()) {
            ++expectedCount;
            beforeInsert.next();
        }
        mDb.begin();
        try (SQLiteStmt stmt = mDb.prepare("INSERT INTO test VALUES(?, ?, ?, ?);")) {
            final Random random = new SecureRandom();
            for (int i = 0; i < 10; ++i) {
                final Model model = Model.create(random);
                stmt.bindNull(1);
                stmt.bindString(2, model.uuid);
                stmt.bindDouble(3, model.weight);
                stmt.bindBlob(4, model.data);
                model.id = stmt.insert();
                stmt.clearBindings();
            }
        }
        mDb.rollback();
        int actualCount = 0;
        final SQLiteIterator afterInsert = mDb.prepare("SELECT * FROM test;").select();
        while (afterInsert.hasNext()) {
            ++actualCount;
            afterInsert.next();
        }
        Assert.assertThat(actualCount, Is.is(expectedCount));
    }

    @After
    public void tearDown() throws Exception {
        mDb.close();
    }

    private static class Model {

        long id;

        String uuid;

        double weight;

        byte[] data;

        static Model create(Random random) {
            final Model model = new Model();
            model.uuid = UUID.randomUUID().toString();
            model.weight = random.nextDouble();
            model.data = new byte[8];
            random.nextBytes(model.data);
            return model;
        }

    }

}
