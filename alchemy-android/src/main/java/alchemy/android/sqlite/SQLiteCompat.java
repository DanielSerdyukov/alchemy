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

package alchemy.android.sqlite;

import alchemy.AlchemyException;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.os.Build;
import android.os.CancellationSignal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class SQLiteCompat {

    private static final SQLiteCursorDriverImpl DRIVER_IMPL;

    private static final SQLiteQueryImpl QUERY_IMPL;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            DRIVER_IMPL = new SQLiteCursorDriverJB();
            QUERY_IMPL = new SQLiteQueryJB();
        } else {
            DRIVER_IMPL = new SQLiteCursorDriverBase();
            QUERY_IMPL = new SQLiteQueryBase();
        }
    }

    private SQLiteCompat() {
    }

    static SQLiteCursorDriver newDriver(SQLiteDatabase db, String sql) {
        return DRIVER_IMPL.newInstance(db, sql);
    }

    static SQLiteQuery newQuery(SQLiteDatabase db, String sql) {
        return QUERY_IMPL.newInstance(db, sql);
    }

    private interface SQLiteCursorDriverImpl {
        SQLiteCursorDriver newInstance(SQLiteDatabase db, String sql);
    }

    private interface SQLiteQueryImpl {
        SQLiteQuery newInstance(SQLiteDatabase db, String sql);
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static final class SQLiteCursorDriverBase implements SQLiteCursorDriverImpl {
        @Override
        public SQLiteCursorDriver newInstance(SQLiteDatabase db, String sql) {
            try {
                final Class<?> clazz = Class.forName("android.database.sqlite.SQLiteDirectCursorDriver");
                final Constructor<?> constructor = clazz.getDeclaredConstructor(
                        SQLiteDatabase.class, String.class, String.class);
                constructor.setAccessible(true);
                return (SQLiteCursorDriver) constructor.newInstance(db, sql, null);
            } catch (ClassNotFoundException e) {
                throw new AlchemyException(e.getMessage(), e.getCause());
            } catch (NoSuchMethodException e) {
                throw new AlchemyException(e.getMessage(), e.getCause());
            } catch (IllegalAccessException e) {
                throw new AlchemyException(e.getMessage(), e.getCause());
            } catch (InstantiationException e) {
                throw new AlchemyException(e.getMessage(), e.getCause());
            } catch (InvocationTargetException e) {
                throw new AlchemyException(e.getMessage(), e.getCause());
            }
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static final class SQLiteCursorDriverJB implements SQLiteCursorDriverImpl {
        @Override
        public SQLiteCursorDriver newInstance(SQLiteDatabase db, String sql) {
            try {
                final Class<?> clazz = Class.forName("android.database.sqlite.SQLiteDirectCursorDriver");
                final Constructor<?> constructor = clazz.getDeclaredConstructor(
                        SQLiteDatabase.class, String.class, String.class, CancellationSignal.class);
                constructor.setAccessible(true);
                return (SQLiteCursorDriver) constructor.newInstance(db, sql, null, null);
            } catch (ClassNotFoundException e) {
                throw new AlchemyException(e.getCause());
            } catch (NoSuchMethodException e) {
                throw new AlchemyException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new AlchemyException(e.getCause());
            } catch (InstantiationException e) {
                throw new AlchemyException(e.getCause());
            } catch (InvocationTargetException e) {
                throw new AlchemyException(e.getCause());
            }
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static final class SQLiteQueryBase implements SQLiteQueryImpl {
        @Override
        public SQLiteQuery newInstance(SQLiteDatabase db, String sql) {
            try {
                final Class<?> clazz = Class.forName("android.database.sqlite.SQLiteQuery");
                final Constructor<?> constructor = clazz.getDeclaredConstructor(
                        SQLiteDatabase.class, String.class, int.class, String[].class);
                constructor.setAccessible(true);
                return (SQLiteQuery) constructor.newInstance(db, sql, 0, null);
            } catch (ClassNotFoundException e) {
                throw new AlchemyException(e.getCause());
            } catch (NoSuchMethodException e) {
                throw new AlchemyException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new AlchemyException(e.getCause());
            } catch (InstantiationException e) {
                throw new AlchemyException(e.getCause());
            } catch (InvocationTargetException e) {
                throw new AlchemyException(e.getCause());
            }
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private static final class SQLiteQueryJB implements SQLiteQueryImpl {
        @Override
        public SQLiteQuery newInstance(SQLiteDatabase db, String sql) {
            try {
                final Class<?> clazz = Class.forName("android.database.sqlite.SQLiteQuery");
                final Constructor<?> constructor = clazz.getDeclaredConstructor(
                        SQLiteDatabase.class, String.class, CancellationSignal.class);
                constructor.setAccessible(true);
                return (SQLiteQuery) constructor.newInstance(db, sql, null);
            } catch (ClassNotFoundException e) {
                throw new AlchemyException(e.getCause());
            } catch (NoSuchMethodException e) {
                throw new AlchemyException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new AlchemyException(e.getCause());
            } catch (InstantiationException e) {
                throw new AlchemyException(e.getCause());
            } catch (InvocationTargetException e) {
                throw new AlchemyException(e.getCause());
            }
        }
    }

}
