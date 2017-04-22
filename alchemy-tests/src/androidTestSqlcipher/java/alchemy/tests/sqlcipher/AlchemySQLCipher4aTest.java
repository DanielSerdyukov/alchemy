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

package alchemy.tests.sqlcipher;

import alchemy.Alchemy;
import alchemy.sqlcipher.SQLCipherSource;
import alchemy.sqlite.DefaultSchema;
import alchemy.tests.AlchemyTest;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import net.sqlcipher.database.SQLiteDatabase;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

@RunWith(AndroidJUnit4.class)
public class AlchemySQLCipher4aTest extends AlchemyTest {

    @BeforeClass
    public static void loadJni() {
        SQLiteDatabase.loadLibs(InstrumentationRegistry.getContext());
    }

    @Override
    protected Alchemy getAlchemy() {
        final File databasePath = InstrumentationRegistry.getContext().getDatabasePath("sqlcipher.db");
        if (databasePath.exists()) {
            Assert.assertThat(databasePath.delete(), Is.is(true));
        }
        return new Alchemy(new SQLCipherSource(new DefaultSchema(1),
                databasePath.getAbsolutePath(), "test123"));
    }

}
