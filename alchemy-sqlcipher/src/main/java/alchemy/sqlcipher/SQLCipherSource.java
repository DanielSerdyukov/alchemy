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

import alchemy.sqlite.SQLiteSource;
import alchemy.sqlite.platform.SQLiteSchema;

public class SQLCipherSource extends SQLiteSource {

    public SQLCipherSource(SQLiteSchema schema, String path, String password) {
        super(new SQLCipherDriver(password), schema, path);
    }

}
