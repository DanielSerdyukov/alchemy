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

package alchemy.tests.sqlite;

import alchemy.Alchemy;
import alchemy.android.sqlite.AndroidSource;
import alchemy.sqlite.DefaultSchema;
import alchemy.tests.AlchemyTest;
import android.support.test.runner.AndroidJUnit4;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AlchemySQLiteTest extends AlchemyTest {

    @Override
    protected Alchemy getAlchemy() {
        return new Alchemy(new AndroidSource(new DefaultSchema(1), ":memory:"));
    }

}
