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

import alchemy.AlchemyException;
import alchemy.sqlite.platform.SQLiteStmt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

@RunWith(MockitoJUnitRunner.class)
public class BinderTest {

    @Mock
    private SQLiteStmt mStmt;

    @Test
    public void bindShort() throws Exception {
        Binder.bind(mStmt, 1, (short) 10);
        Mockito.verify(mStmt).bindLong(1, 10L);
    }

    @Test
    public void bindInteger() throws Exception {
        Binder.bind(mStmt, 2, 100);
        Mockito.verify(mStmt).bindLong(2, 100L);
    }

    @Test
    public void bindLong() throws Exception {
        Binder.bind(mStmt, 3, 1000L);
        Mockito.verify(mStmt).bindLong(3, 1000L);
    }

    @Test
    public void bindFloat() throws Exception {
        Binder.bind(mStmt, 4, 1.23f);
        Mockito.verify(mStmt).bindDouble(4, 1.23f);
    }

    @Test
    public void bindDouble() throws Exception {
        Binder.bind(mStmt, 5, 4.56);
        Mockito.verify(mStmt).bindDouble(5, 4.56);
    }

    @Test
    public void bindString() throws Exception {
        Binder.bind(mStmt, 6, "test");
        Mockito.verify(mStmt).bindString(6, "test");
    }

    @Test
    public void bindBlob() throws Exception {
        Binder.bind(mStmt, 7, new byte[]{1, 2, 3});
        Mockito.verify(mStmt).bindBlob(7, new byte[]{1, 2, 3});
    }

    @Test(expected = AlchemyException.class)
    public void unexpectedType() throws Exception {
        Binder.bind(mStmt, 8, new Date());
    }

}