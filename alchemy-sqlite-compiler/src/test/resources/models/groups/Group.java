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

package alchemy.sqlite.models.groups;

import alchemy.annotations.Column;
import alchemy.annotations.Entry;
import alchemy.annotations.PrimaryKey;
import alchemy.annotations.Relation;
import alchemy.sqlite.models.users.User;

import java.util.List;

@Entry(value = "groups")
public class Group {

    @PrimaryKey
    long mId;

    @Column
    String mName;

    @Relation
    User mAdmin;

    @Relation
    List<User> mUsers;

}
