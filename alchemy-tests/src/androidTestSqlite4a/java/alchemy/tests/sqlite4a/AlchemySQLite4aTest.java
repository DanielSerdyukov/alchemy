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

package alchemy.tests.sqlite4a;

import alchemy.Alchemy;
import alchemy.sqlite.DefaultSchema;
import alchemy.sqlite4a.SQLite4aSource;
import alchemy.tests.AlchemyTest;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.getkeepsafe.relinker.ReLinker;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import sqlite4a.SQLite;

@RunWith(AndroidJUnit4.class)
public class AlchemySQLite4aTest extends AlchemyTest {

    @BeforeClass
    public static void loadJni() {
        ReLinker.loadLibrary(InstrumentationRegistry.getContext(), SQLite.JNI_LIB);
    }

    @Override
    protected Alchemy getAlchemy() {
        return new Alchemy(new SQLite4aSource(new DefaultSchema(1), ":memory:"));
    }

}
